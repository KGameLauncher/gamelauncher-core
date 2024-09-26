package de.dasbabypixel.gamelauncher.api.lifecycle

import java.util.concurrent.atomic.AtomicBoolean

object InitLifecycle {
    private val atomicStarted = AtomicBoolean(false)
    val started
        get() = atomicStarted.get()

    fun init() {
        if (!atomicStarted.compareAndSet(false, true)) throw IllegalStateException("Already started")
        InitLifecycleImplementation().init()
    }
}

expect class InitLifecycleImplementation() {
    fun init()
}