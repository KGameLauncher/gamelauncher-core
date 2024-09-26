package de.dasbabypixel.gamelauncher.api.lifecycle

import java.util.concurrent.atomic.AtomicBoolean

object ShutdownLifecycle {
    private val atomicStopped = AtomicBoolean(false)
    val stopped
        get() = atomicStopped.get()

    fun shutdown() {
        if (!atomicStopped.compareAndSet(false, true)) throw IllegalStateException("Already stopped")
        ShutdownLifecycleImplementation().shutdown()
    }

    fun shutdown(ex: Throwable) {
        if (!atomicStopped.compareAndSet(false, true)) throw IllegalStateException("Already stopped")
        ShutdownLifecycleImplementation().shutdown(ex)
    }
}

expect class ShutdownLifecycleImplementation() {
    fun shutdown()
    fun shutdown(ex: Throwable)
}