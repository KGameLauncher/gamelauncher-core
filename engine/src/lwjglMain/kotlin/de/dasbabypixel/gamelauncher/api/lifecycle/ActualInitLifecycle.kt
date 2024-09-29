package de.dasbabypixel.gamelauncher.api.lifecycle

import de.dasbabypixel.gamelauncher.common.util.concurrent.CommonThreadHelper
import de.dasbabypixel.gamelauncher.lwjgl.started
import de.dasbabypixel.gamelauncher.lwjgl.util.logging.LWJGLLogging

actual class InitLifecycleImplementation {
    actual fun init() {
        superEarlyInit()
        LWJGLLogging.init()
        if (CommonThreadHelper.initialThread != Thread.currentThread()) throw IllegalStateException("Wrong initial thread")
        started()
    }

    private fun superEarlyInit() {
        System.setProperty("jdk.console", "java.base")
    }
}
