package de.dasbabypixel.gamelauncher.api.lifecycle

import de.dasbabypixel.gamelauncher.api.util.extension.getBoolean
import de.dasbabypixel.gamelauncher.common.util.concurrent.CommonThreadHelper
import de.dasbabypixel.gamelauncher.lwjgl.started
import de.dasbabypixel.gamelauncher.lwjgl.util.logging.LWJGLLogging
import java.util.*

actual class InitLifecycleImplementation {
    actual fun init() {
        superEarlyInit()
        LWJGLLogging.init()
        if (CommonThreadHelper.initialThread != Thread.currentThread()) throw IllegalStateException("Wrong initial thread")
        started()
    }

    private fun superEarlyInit() {
        if (!Boolean.getBoolean("gamelauncher.skipsysprops")) {
            val props = (this.javaClass.classLoader.getResourceAsStream("gamelauncher.sysprops")
                ?: throw IllegalStateException("Missing gamelauncher.sysprops")).use {
                val props = Properties()
                props.load(it)
                props
            }
            props.forEach {
                System.setProperty(it.key.toString(), it.value.toString())
            }
        }
    }
}
