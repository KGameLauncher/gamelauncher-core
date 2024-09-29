package de.dasbabypixel.gamelauncher.lwjgl

import de.dasbabypixel.gamelauncher.api.GameLauncher
import de.dasbabypixel.gamelauncher.api.util.Debug
import de.dasbabypixel.gamelauncher.api.util.concurrent.sleep
import de.dasbabypixel.gamelauncher.api.util.function.GameRunnable
import de.dasbabypixel.gamelauncher.api.util.logging.Logging
import de.dasbabypixel.gamelauncher.api.util.logging.getLogger
import de.dasbabypixel.gamelauncher.api.util.resource.AbstractGameResource
import de.dasbabypixel.gamelauncher.lwjgl.window.GLFWThread
import org.jline.jansi.Ansi
import org.lwjgl.glfw.GLFW.*
import java.util.concurrent.CompletableFuture
import kotlin.concurrent.thread


fun main() {
    Logging.out.print(Ansi.ansi().eraseScreen())
    Logging.out.flush()
    GameLauncher.start()
}

fun started() {
    object : AbstractGameResource() {
        override fun cleanup0(): CompletableFuture<Unit>? {
            println("Cleanup")
            return null
        }
    }

    val logger = getLogger<Main>()
    logger.info("In IDE: {}", Debug.inIde)
//    logger.trace("Trace", Exception())
//    logger.debug("Debug")
//    logger.info("Info")
//    logger.warn("Warning", Exception())
//    logger.error("Error", Exception())
//    logger.info(Ansi.ansi().fgRed().a("TestRed").toString())

    println("Test stdout")
    System.err.println("Test stderr")

//    GLFWThread.
    GLFWThread.start()
    GLFWThread.submit(GameRunnable {
        val window = glfwCreateWindow(500, 400, "Test", 0, 0)

        while (!glfwWindowShouldClose(window)) {
            println("Wait")
            glfwWaitEvents()
            println("Update")
            glfwPollEvents()
        }
        println("Destroy")
        glfwDestroyWindow(window)
    })

    thread(isDaemon = false) {
        while (true) {
            sleep(1000)
            println(1234)
//            logger.info("test1")
            sleep(1000)
        }
    }
}

class Main