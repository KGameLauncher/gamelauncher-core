package de.dasbabypixel.gamelauncher.lwjgl.window

import de.dasbabypixel.gamelauncher.api.util.logging.getLogger
import de.dasbabypixel.gamelauncher.lwjgl.Main
import org.lwjgl.glfw.GLFW.*

private val logger = getLogger<GLFWThread>("LWJGL")

class GLFWThread : Thread("GLFW-Thread") {
    companion object {
        fun start() {
            GLFWThread().start()
        }
    }

    override fun run() {
        try {
            glfwInit()
            val window = glfwCreateWindow(100, 100, "Test", 0, 0)
            logger.info("Starting GLFW-Thread")

            while(!glfwWindowShouldClose(window)) {
                glfwWaitEvents()
                glfwPollEvents()
            }
            glfwDestroyWindow(window)
            glfwTerminate()
            Main.shutdown()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}
