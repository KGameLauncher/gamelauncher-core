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
import java.util.concurrent.locks.ReentrantLock

class GLFWRenderThread(group: ThreadGroup, val window: GLFWWindow) :
    AbstractExecutorThread(group, "GLFWRenderThread-${window.id}"), RenderThread {
    private val frameSync = FrameSync()
    private val rendererLock = ReentrantLock()
    private var renderer: RenderImplementationRenderer? = null

    override fun startExecuting() {
        window.creationFuture.join()
        window.makeCurrent()
        GL.createCapabilities()
        GL46.glClearColor(1F, 0F, 0F, 0.5F)
        singleRender()
    }

    fun setRenderer(renderer: RenderImplementationRenderer?) {
        rendererLock.lock()
        try {
            this.renderer = renderer
        } finally {
            rendererLock.unlock()
        }
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

    fun startNextFrame(frames: Int = 1): Long {
        return frameSync.startNextFrame(frames)
    }

    fun awaitFrame(frame: Long) {
        frameSync.waitForFrame(frame)
    }

    public override fun signal() {
        startNextFrame()
    }

    private fun singleRender() {
        rendererLock.lock()
        try {
            val renderer = this.renderer
            renderer?.render(window)
        } finally {
            rendererLock.unlock()
        }
    }

    override fun stopExecuting() {
        GL.setCapabilities(null)
        window.destroyCurrent()
    }
}