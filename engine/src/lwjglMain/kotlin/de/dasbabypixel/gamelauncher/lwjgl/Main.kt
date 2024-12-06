package de.dasbabypixel.gamelauncher.lwjgl

import de.dasbabypixel.gamelauncher.api.GameLauncher
import de.dasbabypixel.gamelauncher.api.util.Debug
import de.dasbabypixel.gamelauncher.api.util.function.GameConsumer
import de.dasbabypixel.gamelauncher.api.util.logging.Logging
import de.dasbabypixel.gamelauncher.api.util.logging.getLogger
import de.dasbabypixel.gamelauncher.lwjgl.window.GLFWThread
import de.dasbabypixel.gamelauncher.lwjgl.window.GLFWWindow
import org.jline.jansi.Ansi
import kotlin.concurrent.thread


fun main(args: Array<String>) {
    Logging.out.print(Ansi.ansi().eraseScreen())
    Logging.out.flush()
    thread(isDaemon = true, priority = Thread.MAX_PRIORITY) {
        Thread.sleep(9223372036854775783)
    }
    GameLauncher.start()
}

var window: GLFWWindow? = null

fun started() {

    val logger = getLogger<Main>()
    logger.info("In IDE: {}", Debug.inIde)

    GLFWThread.startThread()

    val window = GLFWWindow(null)
    de.dasbabypixel.gamelauncher.lwjgl.window = window
    window.requestCloseCallback = GameConsumer {
        it.hide().join()
        GameLauncher.stop()
    }
//    GLFWThread.submit(GameRunnable {
//        throw Exception("Test")
//    })
    window.create()
    window.renderThread.start()
    window.show()
}

fun stopped() {
    window!!.renderThread.cleanup().join()
    GLFWThread.cleanup().join()
}

class Main