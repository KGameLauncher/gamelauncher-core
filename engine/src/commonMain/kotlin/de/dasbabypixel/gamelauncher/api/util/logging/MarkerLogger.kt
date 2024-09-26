package de.dasbabypixel.gamelauncher.api.util.logging

import org.slf4j.Logger
import org.slf4j.Marker
import org.slf4j.event.Level
import org.slf4j.spi.LoggingEventBuilder

class MarkerLogger(private val target: Logger, private val marker: Marker) : Logger {
    override fun getName(): String {
        return target.name
    }

    override fun makeLoggingEventBuilder(level: Level): LoggingEventBuilder {
        return target.makeLoggingEventBuilder(level).addMarker(marker)
    }

    override fun isEnabledForLevel(level: Level): Boolean {
        return target.isEnabledForLevel(level)
    }

    override fun isTraceEnabled(): Boolean {
        return isTraceEnabled(marker)
    }

    override fun trace(msg: String) {
        trace(marker, msg)
    }

    override fun trace(format: String, arg: Any) {
        trace(marker, format, arg)
    }

    override fun trace(format: String, arg1: Any, arg2: Any) {
        trace(marker, format, arg1, arg2)
    }

    override fun trace(format: String, vararg arguments: Any) {
        trace(marker, format, *arguments)
    }

    override fun trace(msg: String, t: Throwable) {
        trace(marker, msg, t)
    }

    override fun isTraceEnabled(marker: Marker): Boolean {
        return target.isTraceEnabled(marker)
    }

    override fun trace(marker: Marker, msg: String) {
        target.trace(marker, msg)
    }

    override fun trace(marker: Marker, format: String, arg: Any) {
        target.trace(marker, format, arg)
    }

    override fun trace(marker: Marker, format: String, arg1: Any, arg2: Any) {
        target.trace(marker, format, arg1, arg2)
    }

    override fun trace(marker: Marker, format: String, vararg argArray: Any) {
        target.trace(marker, format, *argArray)
    }

    override fun trace(marker: Marker, msg: String, t: Throwable) {
        target.trace(marker, msg, t)
    }

    override fun isDebugEnabled(): Boolean {
        return isDebugEnabled(marker)
    }

    override fun debug(msg: String) {
        debug(marker, msg)
    }

    override fun debug(format: String, arg: Any) {
        debug(marker, format, arg)
    }

    override fun debug(format: String, arg1: Any, arg2: Any) {
        debug(marker, format, arg1, arg2)
    }

    override fun debug(format: String, vararg arguments: Any) {
        debug(marker, format, *arguments)
    }

    override fun debug(msg: String, t: Throwable) {
        debug(marker, msg, t)
    }

    override fun isDebugEnabled(marker: Marker): Boolean {
        return target.isDebugEnabled(marker)
    }

    override fun debug(marker: Marker, msg: String) {
        target.debug(marker, msg)
    }

    override fun debug(marker: Marker, format: String, arg: Any) {
        target.debug(marker, format, arg)
    }

    override fun debug(marker: Marker, format: String, arg1: Any, arg2: Any) {
        target.debug(marker, format, arg1, arg2)
    }

    override fun debug(marker: Marker, format: String, vararg arguments: Any) {
        target.debug(marker, format, *arguments)
    }

    override fun debug(marker: Marker, msg: String, t: Throwable) {
        target.debug(marker, msg, t)
    }

    override fun isInfoEnabled(): Boolean {
        return isInfoEnabled(marker)
    }

    override fun info(msg: String) {
        info(marker, msg)
    }

    override fun info(format: String, arg: Any) {
        info(marker, format, arg)
    }

    override fun info(format: String, arg1: Any, arg2: Any) {
        info(marker, format, arg1, arg2)
    }

    override fun info(format: String, vararg arguments: Any) {
        info(marker, format, *arguments)
    }

    override fun info(msg: String, t: Throwable) {
        info(marker, msg, t)
    }

    override fun isInfoEnabled(marker: Marker): Boolean {
        return target.isInfoEnabled(marker)
    }

    override fun info(marker: Marker, msg: String) {
        target.info(marker, msg)
    }

    override fun info(marker: Marker, format: String, arg: Any) {
        target.info(marker, format, arg)
    }

    override fun info(marker: Marker, format: String, arg1: Any, arg2: Any) {
        target.info(marker, format, arg1, arg2)
    }

    override fun info(marker: Marker, format: String, vararg arguments: Any) {
        target.info(marker, format, *arguments)
    }

    override fun info(marker: Marker, msg: String, t: Throwable) {
        target.info(marker, msg, t)
    }

    override fun isWarnEnabled(): Boolean {
        return isWarnEnabled(marker)
    }

    override fun warn(msg: String) {
        warn(marker, msg)
    }

    override fun warn(format: String, arg: Any) {
        warn(marker, format, arg)
    }

    override fun warn(format: String, vararg arguments: Any) {
        warn(marker, format, *arguments)
    }

    override fun warn(format: String, arg1: Any, arg2: Any) {
        warn(marker, format, arg1, arg2)
    }

    override fun warn(msg: String, t: Throwable) {
        warn(marker, msg, t)
    }

    override fun isWarnEnabled(marker: Marker): Boolean {
        return target.isWarnEnabled(marker)
    }

    override fun warn(marker: Marker, msg: String) {
        target.warn(marker, msg)
    }

    override fun warn(marker: Marker, format: String, arg: Any) {
        target.warn(marker, format, arg)
    }

    override fun warn(marker: Marker, format: String, arg1: Any, arg2: Any) {
        target.warn(marker, format, arg1, arg2)
    }

    override fun warn(marker: Marker, format: String, vararg arguments: Any) {
        target.warn(marker, format, *arguments)
    }

    override fun warn(marker: Marker, msg: String, t: Throwable) {
        target.warn(marker, msg, t)
    }

    override fun isErrorEnabled(): Boolean {
        return isErrorEnabled(marker)
    }

    override fun error(msg: String) {
        error(marker, msg)
    }

    override fun error(format: String, arg: Any) {
        error(marker, format, arg)
    }

    override fun error(format: String, arg1: Any, arg2: Any) {
        error(marker, format, arg1, arg2)
    }

    override fun error(format: String, vararg arguments: Any) {
        error(marker, format, *arguments)
    }

    override fun error(msg: String, t: Throwable) {
        error(marker, msg, t)
    }

    override fun isErrorEnabled(marker: Marker): Boolean {
        return target.isErrorEnabled(marker)
    }

    override fun error(marker: Marker, msg: String) {
        target.error(marker, msg)
    }

    override fun error(marker: Marker, format: String, arg: Any) {
        target.error(marker, format, arg)
    }

    override fun error(marker: Marker, format: String, arg1: Any, arg2: Any) {
        target.error(marker, format, arg1, arg2)
    }

    override fun error(marker: Marker, format: String, vararg arguments: Any) {
        target.error(marker, format, *arguments)
    }

    override fun error(marker: Marker, msg: String, t: Throwable) {
        target.error(marker, msg, t)
    }
}
