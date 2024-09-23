package de.dasbabypixel.gamelauncher.lwjgl

import de.dasbabypixel.gamelauncher.api.util.Debug
import de.dasbabypixel.gamelauncher.api.util.logging.Logging
import de.dasbabypixel.gamelauncher.api.util.logging.getLogger
import de.dasbabypixel.gamelauncher.api.util.sleep
import de.dasbabypixel.gamelauncher.lwjgl.init.LWJGLInitHandler
import de.dasbabypixel.gamelauncher.lwjgl.init.LWJGLShutdownHandler
import de.dasbabypixel.gamelauncher.lwjgl.window.GLFWThread
import org.jline.jansi.Ansi
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.concurrent.thread


fun main() {
    Logger.getLogger("org.jline").level = Level.ALL
    Logging.out.print(Ansi.ansi().eraseScreen())
    Logging.out.flush()
    LWJGLInitHandler.init()

    val logger = getLogger<Main>()
    logger.info("In IDE: {}", Debug.runningInIde)
    logger.trace("Trace", Exception())
    logger.debug("Debug")
    logger.info("Info")
    logger.warn("Warning", Exception())
    logger.error("Error", Exception())
    logger.info(Ansi.ansi().fgRed().a("TestRed").toString())

    println("Test stdout")
    System.err.println("Test stderr")

    GLFWThread.start()

    thread(isDaemon = false) {
        while (true) {
            sleep(1000)
            logger.info("test1")
            sleep(1000)
        }
    }
}

class Main {
    companion object {
        fun shutdown(): Nothing {
            LWJGLShutdownHandler.shutdown()
        }
    }
}
