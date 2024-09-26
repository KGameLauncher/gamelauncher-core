package de.dasbabypixel.gamelauncher.api.lifecycle

import de.dasbabypixel.gamelauncher.api.util.logging.getLogger
import de.dasbabypixel.gamelauncher.api.util.resource.ResourceTracker
import kotlin.system.exitProcess

actual class ShutdownLifecycleImplementation {
    companion object {
        val logger = getLogger<ShutdownLifecycleImplementation>()
    }

    actual fun shutdown() {
        shutdown(0)
    }

    actual fun shutdown(ex: Throwable) {
        logger.error(
            "Exception caught, there is a serious problem. Shutting down to prevent dead background processes", ex
        )
        shutdown(-1)
    }

    private fun shutdown(status: Int) {
        ResourceTracker.exit()
        exitProcess(status)
    }
}
