package de.dasbabypixel.gamelauncher.lwjgl.util.debug

import de.dasbabypixel.gamelauncher.api.util.DebugProvider
import de.dasbabypixel.gamelauncher.api.util.Registry
import de.dasbabypixel.gamelauncher.api.util.getBoolean

object LWJGLDebugProvider : DebugProvider {
    override val runningInIde: Boolean = detectRunningInIde()
    override val useAnsi: Boolean = useAnsi()

    private fun useAnsi(): Boolean {
        return detectRunningInIde() || System.console() != null
    }

    private fun detectRunningInIde(): Boolean {
        return Boolean.getBoolean("IN_IDE")
    }

    fun register() {
        Registry.register<DebugProvider>(this)
    }
}