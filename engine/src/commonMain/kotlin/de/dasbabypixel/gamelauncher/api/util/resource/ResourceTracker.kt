package de.dasbabypixel.gamelauncher.api.util.resource

import de.dasbabypixel.gamelauncher.api.util.Debug
import de.dasbabypixel.gamelauncher.api.util.GameException
import de.dasbabypixel.gamelauncher.api.util.logging.getLogger
import java.util.concurrent.ConcurrentHashMap

object ResourceTracker {
    val enabled = Debug.trackResources
    private val resources = ConcurrentHashMap.newKeySet<GameResource>()
    private val logger = getLogger<ResourceTracker>()

    fun startTracking(resource: GameResource) {
        if (!enabled) return
        resources.add(resource)
    }

    fun stopTracking(resource: GameResource) {
        if (!enabled) return
        resources.remove(resource)
    }

    @JvmName("startTrackingResource")
    fun GameResource.startTracking() {
        startTracking(this)
    }

    @JvmName("stopTrackingResource")
    fun GameResource.stopTracking() {
        stopTracking(this)
    }

    fun exit() {
        if (!enabled) return
        for (resource in resources) {
            if (resource is GameResource.StackCapable) {
                val ex = GameException("Stack: ${resource.creationThreadName}")
                ex.stackTrace = resource.creationStack

                logger.error("Memory Leak: {}", resource, ex)
            } else {
                logger.error("Memory Leak: {}", resource)
            }
        }
    }
}