package de.dasbabypixel.gamelauncher.api.lifecycle

import de.dasbabypixel.gamelauncher.api.util.concurrent.AbstractThread
import de.dasbabypixel.gamelauncher.api.util.concurrent.ThreadGroup
import de.dasbabypixel.gamelauncher.api.util.logging.Logging
import de.dasbabypixel.gamelauncher.api.util.logging.getLogger
import de.dasbabypixel.gamelauncher.api.util.resource.ResourceTracker
import de.dasbabypixel.gamelauncher.lwjgl.stopped
import org.apache.logging.log4j.LogManager
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.concurrent.thread
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
        val future = CompletableFuture<Unit>()
        val group = ThreadGroup.create("ShutdownThreads")
        val thread = object : AbstractThread(group, "ShutdownThread") {
            override fun run() {
                this.cleanup()
                try {
                    stopped()
                } catch (e: Throwable) {
                    logger.error("Failed to properly shutdown", e)
                }
                try {
                    ResourceTracker.exit()
                    LogManager.shutdown()
                    Logging.out.println("Shutdown complete")
                } catch (t: Throwable) {
                    t.printStackTrace(Logging.err)
                }
                future.complete(Unit)
            }

            override fun cleanup0(): CompletableFuture<Unit>? = null
        }
        thread.start()
        thread(isDaemon = false, name = "ShutdownThreadController") {
            try {
                future.get(5, TimeUnit.SECONDS)
            } catch (t: TimeoutException) {
                Logging.err.println("Shutdown timeout after 5 seconds")
                val n = Error("Forcible shutting down. Wait for following stack cancelled", t)
                n.stackTrace = thread.stackTrace
                n.printStackTrace(Logging.err)
            } catch (t: Throwable) {
                t.printStackTrace(Logging.err)
            } finally {
                exitProcess(status)
            }
        }
    }
}
