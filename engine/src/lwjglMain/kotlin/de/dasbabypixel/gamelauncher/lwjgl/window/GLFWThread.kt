package de.dasbabypixel.gamelauncher.lwjgl.window

import de.dasbabypixel.gamelauncher.api.util.concurrent.AbstractExecutorThread
import de.dasbabypixel.gamelauncher.api.util.concurrent.ThreadGroup
import de.dasbabypixel.gamelauncher.api.util.logging.getLogger
import de.dasbabypixel.gamelauncher.lwjgl.Main
import org.lwjgl.glfw.GLFW.*

val glThreadGroup = ThreadGroup.create("gl")

object GLFWThread : AbstractExecutorThread(glThreadGroup, "GLFW-Thread") {
    private val logger = getLogger<GLFWThread>("LWJGL")
    override fun run() {
        try {
            println("Init")
            glfwInit()
            println("Window")
            val window = glfwCreateWindow(500, 400, "Test", 0, 0)
            logger.info("Starting GLFW-Thread")

            while (!glfwWindowShouldClose(window)) {
                println("Wait")
                glfwWaitEvents()
                println("Update")
                glfwPollEvents()
            }
            println("Destroy")
            glfwDestroyWindow(window)
            println("Terminate")
            glfwTerminate()
            Main.shutdown()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}
