package de.dasbabypixel.gamelauncher.lwjgl.window

import de.dasbabypixel.gamelauncher.api.render.RenderThread
import de.dasbabypixel.gamelauncher.api.util.concurrent.AbstractExecutorThread
import de.dasbabypixel.gamelauncher.api.util.concurrent.FrameSync
import de.dasbabypixel.gamelauncher.api.util.concurrent.ThreadGroup
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFW.glfwSwapBuffers
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.glVertex2f
import org.lwjgl.opengl.GL46
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.LockSupport

class GLFWRenderThread(group: ThreadGroup, val window: GLFWWindow) :
    AbstractExecutorThread(group, "GLFWRenderThread-${window.id}"), RenderThread {
    private val frameSync = FrameSync()
    private var framebufferUpdate = false
    private var framebufferWidth = 0
    private var framebufferHeight = 0

    override fun startExecuting() {
        window.creationFuture.join()
        window.makeCurrent()
        GL.createCapabilities()
        GL46.glClearColor(1F, 0F, 0F, 0.5F)
        singleRender()
    }

    override fun preLoop() {
        frameSync.syncStart()
    }

    override fun postLoop() {
        frameSync.syncEnd()
    }

    override fun workExecution() {
        singleRender()
    }

    private fun nextFrame(): Long {
        return frameSync.startNextFrame()
    }

    override fun signal() {
        nextFrame()
    }

    fun singleRender() {
        updateViewport()

        val time = (System.currentTimeMillis() % 1000000).toFloat()
        GL46.glClear(GL46.GL_COLOR_BUFFER_BIT)
        GL46.glBegin(GL46.GL_TRIANGLES)
//        glVertex2f(sin(time / 900), cos(time / 2763F))
//        glVertex2f(cos(time / 1300 + 90), cos(time / 1050 + 10))
//        glVertex2f(sin(time / 1620 + 10), sin(time / 1000F))
        glVertex2f(-1F, -1F)
        glVertex2f(0F, 1F)
        glVertex2f(1F, 0F)
        GL46.glEnd()
        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(30))
        glfwSwapBuffers(window.glfwId)
    }

    fun framebufferSize(w: Int, h: Int) {
        lock.lock()
        try {
            framebufferWidth = w
            framebufferHeight = h
            framebufferUpdate = true
            signal()
        } finally {
            lock.unlock()
        }
        GLFW.glfwSetWindowTitle(window.glfwId, "GL: ${w}x$h")
        val frame = frameSync.startNextFrame(1)
//        frameSync.waitForFrame(frame)
    }

    private fun updateViewport() {
        val fbUpdate: Boolean
        val fbw: Int
        val fbh: Int
        lock.lock()
        try {
            fbUpdate = this.framebufferUpdate
            if (fbUpdate) {
                this.framebufferUpdate = false
                fbw = this.framebufferWidth
                fbh = this.framebufferHeight
            } else {
                fbw = 0
                fbh = 0
            }
        } finally {
            lock.unlock()
        }
        if (fbUpdate) {
            GL11.glViewport(0, 0, fbw, fbh)
        }
    }

    override fun stopExecuting() {
        GL.setCapabilities(null)
        window.destroyCurrent()
    }
}