package de.dasbabypixel.gamelauncher.lwjgl.init

import de.dasbabypixel.gamelauncher.lwjgl.util.debug.LWJGLDebugProvider
import de.dasbabypixel.gamelauncher.lwjgl.util.logging.LWJGLLogging

object LWJGLInitHandler {
    fun init() {
        initProperties()
        initDebug()
        initLogging()
    }

    private fun initProperties() {
        System.setProperty("jdk.console", "java.base")
    }

    private fun initDebug() {
        LWJGLDebugProvider.register()
    }

    private fun initLogging() {
        LWJGLLogging.init()
    }
}