package de.dasbabypixel.gamelauncher.lwjgl.init

import kotlin.system.exitProcess

object LWJGLShutdownHandler {
    fun shutdown(): Nothing {
        exitProcess(0)
    }
}