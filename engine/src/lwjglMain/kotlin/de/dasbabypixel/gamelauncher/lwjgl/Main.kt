package de.dasbabypixel.gamelauncher.lwjgl

import de.dasbabypixel.gamelauncher.api.GameLauncher
import de.dasbabypixel.gamelauncher.api.util.Debug
import de.dasbabypixel.gamelauncher.api.util.concurrent.sleep
import de.dasbabypixel.gamelauncher.api.util.function.GameConsumer
import de.dasbabypixel.gamelauncher.api.util.logging.getLogger
import de.dasbabypixel.gamelauncher.api.util.resource.AbstractGameResource
import de.dasbabypixel.gamelauncher.lwjgl.window.GLFWThread
import de.dasbabypixel.gamelauncher.lwjgl.window.GLFWWindow
import org.jline.reader.LineReaderBuilder
import org.jline.terminal.TerminalBuilder
import java.util.concurrent.CompletableFuture
import kotlin.concurrent.thread


fun main(args: Array<String>) {
    val terminal = TerminalBuilder.terminal()
    val reader = LineReaderBuilder.builder().terminal(terminal).build()
    println(terminal)
    println(reader)
    val line = reader.readLine()
    println(line)
//    Logging.out.print(Ansi.ansi().eraseScreen())
//    Logging.out.flush()
//    Logging.out.println("ää")
//    Logging.out.println(Logging.out.charset())
//    Logging.out.write("ää\n".toByteArray(Charset.defaultCharset()))
//    Logging.out.write("ää\n".toByteArray(Charsets.UTF_8))
//    Logging.out.println(System.getProperty("native.encoding"))
//    Logging.out.println(System.getProperty("file.encoding"))
//    System.getProperties().forEach { t, u -> println("$t: $u") }
//    GameLauncher.start()
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
    GLFWThread.startThread()
//    GLFWThread.submit(GameRunnable {
//        val window = glfwCreateWindow(500, 400, "Test", 0, 0)
//
//        while (!glfwWindowShouldClose(window)) {
//            println("Wait")
//            glfwWaitEvents()
//            println("Update")
//            glfwPollEvents()
//        }
//        println("Destroy")
//        glfwDestroyWindow(window)
//    })

    val window = GLFWWindow(null)
    window.requestCloseCallback = GameConsumer {
        it.hide().join()
        GameLauncher.stop()
    }
//    GLFWThread.submit(GameRunnable {
//        throw Exception("Test")
//    })
    window.create()
    window.show()

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