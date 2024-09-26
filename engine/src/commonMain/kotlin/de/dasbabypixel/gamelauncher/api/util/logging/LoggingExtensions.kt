package de.dasbabypixel.gamelauncher.api.util.logging

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.Marker
import org.slf4j.MarkerFactory

inline fun <reified T : Any> getLogger(): Logger {
    return LoggerFactory.getLogger(T::class.java)
}

inline fun <reified T : Any> getLogger(marker: String): Logger {
    return getLogger<T>().withDefaultMarker(marker)
}

fun Logger.withDefaultMarker(marker: String): Logger {
    return withDefaultMarker(MarkerFactory.getMarker(marker))
}

fun Logger.withDefaultMarker(marker: Marker): Logger {
    return MarkerLogger(this, marker)
}

