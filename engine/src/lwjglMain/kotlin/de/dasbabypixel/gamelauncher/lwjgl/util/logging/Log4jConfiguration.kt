package de.dasbabypixel.gamelauncher.lwjgl.util.logging

import de.dasbabypixel.gamelauncher.api.util.Color
import de.dasbabypixel.gamelauncher.api.util.Registry
import de.dasbabypixel.gamelauncher.api.util.logging.*
import de.dasbabypixel.gamelauncher.common.util.logging.CommonPatternProvider
import de.dasbabypixel.gamelauncher.common.util.logging.LoggingPrintStream
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.Appender
import org.apache.logging.log4j.core.Core
import org.apache.logging.log4j.core.Filter
import org.apache.logging.log4j.core.config.Configuration
import org.apache.logging.log4j.core.config.Configurator
import org.apache.logging.log4j.core.config.builder.api.*
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration
import org.apache.logging.log4j.core.config.plugins.processor.PluginEntry
import org.apache.logging.log4j.core.config.plugins.util.PluginRegistry
import org.apache.logging.log4j.core.config.plugins.util.PluginType
import org.jline.reader.LineReader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.PrintStream

object Log4jConfiguration {
    init {
        Registry.register<LoggingPrintStream.Platform>(object : LoggingPrintStream.Platform {
            val walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
            val osc = LoggingPrintStream.OutputStreamConverter::class.java.name
            val drop = listOf(
                OutputStream::class, PrintStream::class, OutputStreamWriter::class, Throwable::class, ThreadGroup::class
            ).map { it.java.name }.plus(listOf("sun.nio.cs.StreamEncoder", "java.lang.Throwable\$WrappedPrintStream"))
                .toSet()

            override fun createInstance(logger: Logger, printLocation: Boolean): LoggingPrintStream.PlatformInstance {
                return object : LoggingPrintStream.PlatformInstance {
                    val logger = LogManager.getLogger(logger.name)!!
                    val level = this.logger.level!!
                    override fun log(message: String) {
                        val location = findCaller()
                        this.logger.atLevel(level).withLocation(location).log(message)
                    }

                    private fun findCaller(): StackTraceElement {
                        return walker.walk { s ->
                            s.dropWhile { f ->
                                f.className != osc
                            }.dropWhile { f ->
                                f.className == osc
                            }.dropWhile { f ->
                                drop.contains(f.className)
                            }.findFirst()
                        }.map { it.toStackTraceElement() }.orElseThrow()
                    }
                }
            }
        })
    }

    private fun ConfigurationBuilder<*>.layout(
        pattern: CharSequence, disableAnsi: Boolean
    ): LayoutComponentBuilder {
        val layout = newLayout("PatternLayout")
        layout.addAttribute("pattern", pattern)
        layout.addAttribute("alwaysWriteExceptions", false)
        layout.addAttribute("disableAnsi", disableAnsi)
        return layout
    }

    private fun ConfigurationBuilder<*>.terminalConsole(name: String, pattern: String, disableAnsi: Boolean) {
        val appender = newAppender(name, "TerminalConsole")
        val layout = layout(pattern, disableAnsi)
        appender.add(layout)
        add(appender)
    }

    private fun ConfigurationBuilder<*>.file(name: String, pattern: String) {
        val appender = newAppender(name, "File")
        appender.addAttribute("fileName", "latest.log")

        val layout = layout(pattern, disableAnsi = true)
        appender.add(layout)
        add(appender)
    }

    private fun ConfigurationBuilder<*>.filter(
        plugin: String,
        vararg attributes: Pair<String, String>,
        onMatch: Filter.Result = Filter.Result.ACCEPT,
        onMismatch: Filter.Result = Filter.Result.DENY
    ) = newFilter(plugin, onMatch, onMismatch).apply {
        attributes.forEach { addAttribute(it.first, it.second) }
    }

    private fun ConfigurationBuilder<*>.appenderRef(
        logger: RootLoggerComponentBuilder,
        ref: String,
        filterPlugin: String,
        vararg attributes: Pair<String, String>,
        onMatch: Filter.Result = Filter.Result.ACCEPT,
        onMismatch: Filter.Result = Filter.Result.DENY
    ) {
        val filter = filter(filterPlugin, attributes = attributes, onMatch, onMismatch)
        logger.add(newAppenderRef(ref).add(filter))
    }

    private fun LoggerComponentBuilder.configureOut(): LoggerComponentBuilder {
        return addAttribute("includeLocation", true).addAttribute("additivity", false)
    }

    private fun createConfiguration(useAnsi: Boolean, lineReader: LineReader): Configuration {
        val terminalAppenderEntry = PluginEntry()
        terminalAppenderEntry.category = Core.CATEGORY_NAME
        terminalAppenderEntry.key = TerminalConsoleAppender.NAME.lowercase()
        terminalAppenderEntry.name = TerminalConsoleAppender.NAME
        terminalAppenderEntry.isPrintable = true
        terminalAppenderEntry.className = TerminalConsoleAppender::class.java.name
        val terminalAppenderType =
            PluginType(terminalAppenderEntry, TerminalConsoleAppender::class.java, Appender.ELEMENT_TYPE)
        PluginRegistry.getInstance().loadFromMainClassLoader()[Core.CATEGORY_NAME.lowercase()]!!.add(
            terminalAppenderType
        )
        TerminalConsoleAppender.lineReader = lineReader
        val disableAnsi = !useAnsi
        val builder = ConfigurationBuilderFactory.newConfigurationBuilder()

        val levelList = LogLevelRegistry.levels()
        val logger = builder.newAsyncRootLogger(Level.ALL)
        val stdout = builder.newAsyncLogger("stdout", Level.ALL).configureOut()
        val stderr = builder.newAsyncLogger("stderr", Level.ALL).configureOut()
        val markers = levelList.filter { it is LogLevel.Colored || it is LogLevel.Custom || it is LogLevel.Pattern }
            .map { it.marker }
        levelList.forEach { level ->
            val pattern = convertToPattern(level)
            val name = when (level) {
                LogLevel.Root -> "ROOT"
                LogLevel.Stderr -> "stderr"
                LogLevel.Stdout -> "stdout"
                is LogLevel.Colored, is LogLevel.Custom, is LogLevel.Pattern -> level.marker
            }
            val nameFile = name + "File"
            builder.terminalConsole(name, pattern, disableAnsi)
            builder.file(nameFile, pattern)
            when (level) {
                LogLevel.Root -> {
                    val appenderRef: AppenderRefComponentBuilder = builder.newAppenderRef(name)
                    val appenderRefFile = builder.newAppenderRef(nameFile)

                    val allFilters = markers.map { marker ->
                        builder.filter(
                            "MarkerFilter",
                            "marker" to marker,
                            onMatch = Filter.Result.DENY,
                            onMismatch = Filter.Result.NEUTRAL
                        )
                    }
                    allFilters.apply {
                        if (size >= 2) {
                            val filters = builder.newComponent("Filters")
                            forEach {
                                filters.addComponent(it)
                            }
                            appenderRef.addComponent(filters)
                            appenderRefFile.addComponent(filters)
                        } else if (size == 1) {
                            appenderRef.addComponent(this[0])
                            appenderRefFile.addComponent(this[0])
                        }
                    }
                    logger.add(appenderRef)
                    logger.add(appenderRefFile)
                }

                LogLevel.Stderr -> {
                    stderr.add(builder.newAppenderRef(name))
                    stderr.add(builder.newAppenderRef(nameFile))
                }

                LogLevel.Stdout -> {
                    stdout.add(builder.newAppenderRef(name))
                    stdout.add(builder.newAppenderRef(nameFile))
                }

                is LogLevel.Colored, is LogLevel.Custom, is LogLevel.Pattern -> {
                    builder.appenderRef(logger, name, "MarkerFilter", "marker" to name)
                    builder.appenderRef(logger, nameFile, "MarkerFilter", "marker" to name)
                }
            }
        }

//        builder.setPackages(TerminalConsoleAppender::class.java.packageName)
        builder.add(logger)
        builder.add(stdout)
        builder.add(stderr)

        val conf = builder.build(false)
        return conf
    }

    private fun convertToPattern(logLevel: LogLevel): String {
        val pattern = CustomPatterns.pattern(logLevel)
        val parse = PatternParser.parse(pattern)
        return Log4jPatternSerializer.serialize(parse)
    }

    fun setup(useAnsi: Boolean, lineReader: LineReader) {
        LWJGLPatternProvider.register()
        LWJGLLogLevels.register()
        val configuration = createConfiguration(useAnsi, lineReader) as BuiltConfiguration
        Configurator.reconfigure(configuration)
        System.setOut(LoggingPrintStream(LoggerFactory.getLogger("stdout")))
        System.setErr(LoggingPrintStream(LoggerFactory.getLogger("stderr")))
    }
}

object LWJGLLogLevels {
    private val FG_LWJGL = Color(0, 255, 255).styleHex
    fun register() {
        LogLevelRegistry.registerLevel(LogLevel.Colored("LWJGL", FG_LWJGL))
    }
}

object LWJGLPatternProvider {
    fun register() {
        CommonPatternProvider.register()
        CommonPatternProvider.freeze()
    }
}
