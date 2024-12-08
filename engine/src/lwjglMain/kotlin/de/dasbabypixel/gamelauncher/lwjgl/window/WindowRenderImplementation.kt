package de.dasbabypixel.gamelauncher.lwjgl.window

import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback
import org.lwjgl.glfw.GLFW.glfwSwapBuffers
import org.lwjgl.glfw.GLFWFramebufferSizeCallbackI
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.glVertex2f
import org.lwjgl.opengl.GL46
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.LockSupport
import java.util.concurrent.locks.ReentrantLock

interface WindowRenderImplementation {
    fun enable(window: GLFWWindow): RenderImplementationRenderer
    fun disable(window: GLFWWindow, renderer: RenderImplementationRenderer)
}

interface RenderImplementationRenderer {
    fun render(window: GLFWWindow)
}


class DoubleBufferedAsyncRenderImpl : WindowRenderImplementation {


    override fun enable(window: GLFWWindow): RenderImplementationRenderer {
        return DoubleBufferedAsyncRenderer(window)
    }

    override fun disable(window: GLFWWindow, renderer: RenderImplementationRenderer) {
        renderer as DoubleBufferedAsyncRenderer
        renderer.disable()
    }

    class DoubleBufferedAsyncRenderer(val window: GLFWWindow) : RenderImplementationRenderer {
        val lock = ReentrantLock()
        private var framebufferUpdate = false
        private var framebufferWidth = 0
        private var framebufferHeight = 0
        private var viewportWidth = 0
        private var viewportHeight = 0

        init {
            glfwSetFramebufferSizeCallback(window.glfwId) { _, w, h ->
                lock.lock()
                try {
                    framebufferWidth = w
                    framebufferHeight = h
                    framebufferUpdate = true
                } finally {
                    lock.unlock()
                }
                window.renderThread.signal()
                val frame = window.renderThread.startNextFrame()
                GLFW.glfwSetWindowTitle(window.glfwId, "GL $w $h")
            }
        }

        fun disable() {
            glfwSetFramebufferSizeCallback(window.glfwId, null)
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
            viewportWidth = fbw
            viewportHeight = fbh
            if (fbUpdate) {
                GL11.glViewport(0, 0, viewportWidth, viewportHeight)
            }
        }

        override fun render(window: GLFWWindow) {
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
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(500))
            println("SWAP $viewportWidth $viewportHeight")
            glfwSwapBuffers(window.glfwId)
        }

    }
}