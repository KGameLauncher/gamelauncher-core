package de.dasbabypixel.gamelauncher.lwjgl.window

import de.dasbabypixel.gamelauncher.api.GameLauncher
import de.dasbabypixel.gamelauncher.api.util.GameException
import de.dasbabypixel.gamelauncher.api.util.concurrent.Executor
import de.dasbabypixel.gamelauncher.api.util.concurrent.Thread
import de.dasbabypixel.gamelauncher.api.util.concurrent.ThreadGroup
import de.dasbabypixel.gamelauncher.api.util.concurrent.currentThread
import de.dasbabypixel.gamelauncher.api.util.function.GameCallable
import de.dasbabypixel.gamelauncher.api.util.function.GameConsumer
import de.dasbabypixel.gamelauncher.api.util.logging.getLogger
import de.dasbabypixel.gamelauncher.api.window.Window
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.system.MemoryUtil
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock

class GLFWWindow : Window {
    companion object {
        private val logger = getLogger<GLFWWindow>("LWJGL")
        private val idCounter = AtomicInteger()
        private val currentWindow = ThreadLocal<GLFWWindow>()
    }

    private val sharedWindows: SharedWindows
    private val modifyLock = ReentrantLock()
    val id = idCounter.incrementAndGet()
    private val parent: GLFWWindow?
    private var owned = false
    val renderThread: GLFWRenderThread
    private val group: ThreadGroup
    val creationFuture = CompletableFuture<Unit>()
    var requestCloseCallback: GameConsumer<GLFWWindow>? = null
    var glfwId: Long = 0L
        private set

    internal constructor(parent: GLFWWindow?) {
        this.parent = parent
        this.sharedWindows = parent?.sharedWindows ?: SharedWindows()
        this.sharedWindows.add(this)
        this.group = ThreadGroup.create("GLFWWindow-${id}", parent?.group ?: currentThread().group)
        this.renderThread = GLFWRenderThread(group, this)
    }

    internal fun create(): CompletableFuture<Unit> {
        return runWindow { GLFWWindowCreator().run() }
    }

    override fun show(): CompletableFuture<Unit> {
        return runWindow { glfwShowWindow(it) }
    }

    override fun hide(): CompletableFuture<Unit> {
        return runWindow { glfwHideWindow(it) }
    }

    private inline fun <T> runWindow(crossinline function: (Long) -> T): CompletableFuture<T> {
        return GLFWThread.submit(GameCallable { function(glfwId) })
    }

    fun makeCurrent() {
        val thread = currentThread()
        if (thread !is Executor) throw IllegalThreadStateException("Thread must be an executor thread to make a window current")
        val lock = this.modifyLock
        lock.lock()
        try {
            if (this.owned) throw IllegalStateException("Already owned")
            this.owned = true
        } finally {
            lock.unlock()
        }
        glfwMakeContextCurrent(glfwId)
        currentWindow.set(this)
    }

    fun destroyCurrent() {
        val thread = currentThread()
        if (thread !is Executor) throw IllegalThreadStateException("Owner thread is not an executor thread. This should not be possible")
        if (currentWindow.get() != this) throw IllegalThreadStateException("Tried to release context from other thread than owner thread")
        if (!this.owned) throw IllegalStateException("Not currently owned. Should be impossible")
        val lock = this.modifyLock
        lock.lock()
        try {
            this.owned = false
        } finally {
            lock.unlock()
        }
        glfwMakeContextCurrent(0L)
        currentWindow.remove()
    }

    private fun glfwError(): Nothing {
        val buf = MemoryUtil.memAllocPointer(1)
        glfwGetError(buf)
        val error = buf.stringUTF8
        MemoryUtil.memFree(buf)
        throw GameException("GLFW Error: $error")
    }

    private inner class GLFWWindowCreator : Runnable {

        override fun run() {
            try {
                glfwDefaultWindowHints()
                glfwWindowHint(GLFW_SCALE_TO_MONITOR, GLFW_TRUE)
                glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
                glfwWindowHint(GLFW_TRANSPARENT_FRAMEBUFFER, GLFW_TRUE)
                glfwWindowHint(GLFW_FOCUS_ON_SHOW, GLFW_FALSE)

                val primaryMonitorId = glfwGetPrimaryMonitor()
                val primaryMode = glfwGetVideoMode(primaryMonitorId)!!
                glfwWindowHint(GLFW_RED_BITS, primaryMode.redBits())
                glfwWindowHint(GLFW_BLUE_BITS, primaryMode.blueBits())
                glfwWindowHint(GLFW_GREEN_BITS, primaryMode.greenBits())
                glfwWindowHint(GLFW_REFRESH_RATE, primaryMode.refreshRate())

                val startWidth = primaryMode.width() / 2
                val startHeight = primaryMode.height() / 2

                glfwId = glfwCreateWindow(startWidth, startHeight, "GameLauncher", 0, 0)
                if (glfwId == 0L) glfwError()

                glfwSetWindowSizeLimits(glfwId, 1, 1, GLFW_DONT_CARE, GLFW_DONT_CARE)

                glfwSetWindowCloseCallback(glfwId) {
                    val cb = requestCloseCallback ?: return@glfwSetWindowCloseCallback
                    try {
                        cb.accept(this@GLFWWindow)
                    } catch (t: Throwable) {
                        GameLauncher.handleException(t)
                    }
                }
            } catch (t: Throwable) {
                creationFuture.completeExceptionally(t)
                throw t
            }
            creationFuture.complete(Unit)
        }
    }
}

private class SharedWindows {
    private val sharedWindows: CopyOnWriteArrayList<GLFWWindow> = CopyOnWriteArrayList()
    val lock = ReentrantLock()

    fun add(window: GLFWWindow) {
        sharedWindows.add(window)
    }
}