package de.dasbabypixel.gamelauncher.api.util.resource

import de.dasbabypixel.gamelauncher.api.util.GameException
import de.dasbabypixel.gamelauncher.api.util.concurrent.Thread
import de.dasbabypixel.gamelauncher.api.util.concurrent.currentThread
import de.dasbabypixel.gamelauncher.api.util.logging.getLogger
import de.dasbabypixel.gamelauncher.api.util.resource.ResourceTracker.startTracking
import de.dasbabypixel.gamelauncher.api.util.resource.ResourceTracker.stopTracking
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean

abstract class AbstractGameResource : GameResource.StackCapable {
    companion object {
        private val logger = getLogger<AbstractGameResource>()
    }

    final override var creationStack: Array<StackTraceElement>? = null
        private set
    final override var creationThreadName: String? = null
        private set
    final override var cleanupStack: Array<StackTraceElement>? = null
        private set
    final override var cleanupThreadName: String? = null
        private set

    private val created = AtomicBoolean(false)
    private val calledCleanup = AtomicBoolean(false)
    private val stackAvailable = AtomicBoolean(false)

    final override val cleanedUp: Boolean
        get() = cleanupFuture.isDone
    final override val cleanupFuture: CompletableFuture<Unit> = CompletableFuture()

    open val autoTrack
        get() = true

    init {
        if (ResourceTracker.enabled) {
            @Suppress("LeakingThis")
            if (this.autoTrack) {
                track()
            }
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    protected fun track(thread: Thread = currentThread()) {
        if (!created.compareAndSet(false, true)) throw IllegalStateException("Already tracked")
        creationThreadName = thread.name
        creationStack = thread.stackTrace
        startTracking()

        cleanupFuture.whenComplete { _, _ ->
            stopTracking()
        }
    }

    protected abstract fun cleanup0(): CompletableFuture<Unit>?

    final override fun cleanup(): CompletableFuture<Unit> {
        if (!created.get()) throw IllegalStateException("Resource was never tracked")
        if (calledCleanup.compareAndSet(false, true)) {
            if (ResourceTracker.enabled) {
                val thread = currentThread()
                cleanupStack = thread.stackTrace
                cleanupThreadName = thread.name
            }
            val f = try {
                cleanup0()
            } catch (ex: Throwable) {
                logger.error("Failed to cleanup GameResource", ex)
                null
            }
            if (f == null) {
                stopTracking()
                cleanupFuture.complete(Unit)
            } else {
                f.whenComplete { _, t ->
                    stopTracking()
                    if (t != null) cleanupFuture.completeExceptionally(t)
                    else cleanupFuture.complete(Unit)
                }
            }
        } else {
            val ex = GameException("Multiple cleanups")
            if (ResourceTracker.enabled) {
                val creation = GameException("CreationStack: $creationThreadName")
                creation.stackTrace = creationStack
                val cleanup = GameException("CleanupStack: $cleanupThreadName")
                cleanup.stackTrace = cleanupStack
                ex.addSuppressed(creation)
                ex.addSuppressed(cleanup)
            }
            logger.error("Failed to cleanup GameResource", ex)
            stopTracking() // Stop tracking, we already printed to console
        }
        return cleanupFuture
    }
}