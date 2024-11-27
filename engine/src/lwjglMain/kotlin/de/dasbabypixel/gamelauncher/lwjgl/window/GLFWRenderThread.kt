package de.dasbabypixel.gamelauncher.lwjgl.window

import de.dasbabypixel.gamelauncher.api.render.RenderThread
import de.dasbabypixel.gamelauncher.api.util.concurrent.AbstractExecutorThread
import de.dasbabypixel.gamelauncher.api.util.concurrent.ThreadGroup
import de.dasbabypixel.gamelauncher.api.util.concurrent.sleep
import org.lwjgl.glfw.GLFW.glfwSwapBuffers
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.glVertex2f
import org.lwjgl.opengl.GL46

class GLFWRenderThread(group: ThreadGroup, val window: GLFWWindow) :
    AbstractExecutorThread(group, "GLFWRenderThread-${window.id}"), RenderThread {
    override fun startExecuting() {
        window.creationFuture.join()
        window.makeCurrent()
        GL.createCapabilities()
        GL46.glClearColor(1F,0F,0F,0.5F)

    }

    override fun workExecution() {
        println("1")
        singleRender()

    }

    fun singleRender() {
        GL46.glClear(GL46.GL_COLOR_BUFFER_BIT)
//        GL46.glBegin(GL46.GL_TRIANGLES)
//        glVertex2f(0F, 0F)
//        glVertex2f(1F,0F)
//        glVertex2f(1F,1F)
//        GL46.glEnd()
        glfwSwapBuffers(window.glfwId)
    }

    override fun waitForSignal() {
        super.waitForSignal()
    }

    override fun signal() {
        super.signal()
    }

    override fun awaitWork() {
        super.awaitWork()
    }

    override fun stopExecuting() {
        GL.setCapabilities(null)
        window.destroyCurrent()
    }
}