package de.dasbabypixel.gamelauncher.api.util

@Suppress("unused")
val Debug = Registry.instance<DebugProvider>()

interface DebugProvider {
    val runningInIde: Boolean
    val useAnsi: Boolean
}