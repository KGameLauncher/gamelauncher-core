package de.dasbabypixel.gamelauncher.lwjgl

import de.dasbabypixel.gamelauncher.api.GameLauncher
import de.dasbabypixel.gamelauncher.api.util.Debug
import de.dasbabypixel.gamelauncher.api.util.function.GameConsumer
import de.dasbabypixel.gamelauncher.api.util.logging.Logging
import de.dasbabypixel.gamelauncher.api.util.logging.getLogger
import de.dasbabypixel.gamelauncher.lwjgl.window.GLFWThread
import de.dasbabypixel.gamelauncher.lwjgl.window.GLFWWindow
import org.jline.jansi.Ansi


fun main(args: Array<String>) {
    Logging.out.print(Ansi.ansi().eraseScreen())
    Logging.out.flush()
    GameLauncher.start()
}

fun started() {

    val logger = getLogger<Main>()
    logger.info("In IDE: {}", Debug.inIde)

    GLFWThread.startThread()

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
}

fun stopped() {
    GLFWThread.cleanup().join()
}

class Main