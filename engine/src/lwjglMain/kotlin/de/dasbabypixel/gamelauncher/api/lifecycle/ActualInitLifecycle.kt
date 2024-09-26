package de.dasbabypixel.gamelauncher.api.lifecycle

import de.dasbabypixel.gamelauncher.lwjgl.started
import de.dasbabypixel.gamelauncher.lwjgl.util.logging.LWJGLLogging

actual class InitLifecycleImplementation {
    actual fun init() {
        superEarlyInit()
        LWJGLLogging.init()
        started()
    }

    private fun superEarlyInit() {
        System.setProperty("jdk.console", "java.base")
    }
}
