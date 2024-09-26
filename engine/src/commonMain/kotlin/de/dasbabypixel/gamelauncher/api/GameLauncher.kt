package de.dasbabypixel.gamelauncher.api

import de.dasbabypixel.gamelauncher.api.lifecycle.InitLifecycle
import de.dasbabypixel.gamelauncher.api.lifecycle.ShutdownLifecycle
import de.dasbabypixel.gamelauncher.api.util.logging.getLogger

abstract class GameLauncher {
    companion object {
        private val logger = getLogger<GameLauncher>()

        val started: Boolean
            get() = InitLifecycle.started

        fun start() {
            InitLifecycle.init()
        }

        fun stop() {
            ShutdownLifecycle.shutdown()
        }

        fun handleException(ex: Throwable) {
            ShutdownLifecycle.shutdown(ex)
        }
    }
}