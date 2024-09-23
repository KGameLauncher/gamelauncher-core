package de.dasbabypixel.gamelauncher.lwjgl.util.logging

import org.apache.logging.log4j.core.*
import org.apache.logging.log4j.core.appender.AbstractAppender
import org.apache.logging.log4j.core.config.Property
import org.apache.logging.log4j.core.config.plugins.Plugin
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory
import org.apache.logging.log4j.core.impl.LocationAware
import org.jline.reader.LineReader
import java.io.Serializable

@Plugin(
    name = TerminalConsoleAppender.NAME,
    category = Core.CATEGORY_NAME,
    elementType = Appender.ELEMENT_TYPE,
    printObject = true
)
class TerminalConsoleAppender(
    name: String,
    filter: Filter?,
    layout: Layout<out Serializable?>?,
    ignoreExceptions: Boolean,
    properties: Array<Property?>?,
    private val reader: LineReader
) : AbstractAppender(name, filter, layout, ignoreExceptions, properties), LocationAware {

    override fun append(event: LogEvent) {
        print(layout.toSerializable(event).toString())
    }

    private fun print(text: String) {
        reader.printAbove(text)
    }

    class Builder<B : Builder<B>?> : AbstractAppender.Builder<B>(),
        org.apache.logging.log4j.core.util.Builder<TerminalConsoleAppender?> {
        override fun build(): TerminalConsoleAppender {
            return TerminalConsoleAppender(
                name, filter, getOrCreateLayout(), isIgnoreExceptions, propertyArray, lineReader!!
            )
        }

        override fun getErrorPrefix(): String = super<AbstractAppender.Builder>.getErrorPrefix()
    }

    companion object {
        const val NAME: String = "TerminalConsole"
        var lineReader: LineReader? = null

        @JvmStatic
        @PluginBuilderFactory
        fun <B : Builder<B>> newBuilder(): Builder<B> {
            return Builder()
        }
    }
}
