package de.dasbabypixel.gamelauncher.api.util.logging

import de.dasbabypixel.gamelauncher.api.util.Color
import de.dasbabypixel.gamelauncher.api.util.Registry
import de.dasbabypixel.gamelauncher.api.util.logging.CustomPattern.PlatformProvider
import java.util.concurrent.atomic.AtomicBoolean

sealed class LogLevel(
    val marker: String
) {
    class Colored(marker: String, val fgStyle: String? = null, val bgStyle: String? = null) : LogLevel(marker)
    class Pattern(marker: String, val pattern: String) : LogLevel(marker)
    class Custom(marker: String) : LogLevel(marker)
    data object Stdout : LogLevel("")
    data object Stderr : LogLevel("")
    data object Root : LogLevel("")
}

object LogLevelRegistry {
    private val levels = ArrayList<LogLevel>()
    private val frozen = AtomicBoolean(false)

    init {
        levels.add(LogLevel.Root)
        levels.add(LogLevel.Stdout)
        levels.add(LogLevel.Stderr)
    }

    fun registerLevel(level: LogLevel) {
        if (frozen.get()) throw IllegalStateException("LevelRegistry is frozen")
        if (level.marker.isEmpty()) throw IllegalArgumentException("LogLevel with empty marker is not allowed")
        if (levels.any { it.marker == level.marker }) throw IllegalStateException("Level with marker ${level.marker} already registered")
        levels.add(level)
    }

    fun levels(): List<LogLevel> {
        frozen.set(true)
        return levels
    }
}

object PatternRegistry {
    private val frozen = AtomicBoolean(false)
    private val provider = Registry.instance<PlatformProvider>()
    private val patterns = HashMap<String, CustomPattern>()

    fun registerPattern(pattern: CustomPattern) {
        if (frozen.get()) throw IllegalStateException("PatternRegistry is frozen")
        if (pattern.simplifier == CustomPattern.NativeSimplifier && !provider.isNative(pattern.name)) {
            throw PatternException("Tried to illegally inject native pattern. This mustn't be done because of logging implementation limits")
        }
        patterns[pattern.name] = pattern
    }

    internal fun freeze() {
        frozen.set(true)
    }

    fun pattern(type: String): CustomPattern {
        return patterns[type] ?: throw PatternException("Pattern with type $type not found")
    }
}

object PatternParser {
    const val BEGIN_FORMAT = '%'
    const val BEGIN_FORMAT_CONTENT = '{'
    const val END_FORMAT_CONTENT = '}'
    const val END_FORMAT = '$'

    fun parse(input: String): ParseResult {
        // All the root level results
        val state = State()
        state.pushParser(Parser.Text())
        try {
            for (char in input) {
                state.parserState.parse(state, char)
                state.cursor++
            }

            while (state.parserStates.size > 0) {
                state.parserState.tryEnd(state)
            }

            return state.results.build
        } catch (e: PatternException) {
            throw PatternException(e, " at cursor ${state.cursor} for \"$input\"")
        }
    }

    private val List<ParseResult>.build: ParseResult
        get() = if (size == 1) this[0] else if (isEmpty()) ParseResult.Empty else ParseResult.Multi(this)

    private sealed interface Parser {
        fun parse(state: State, char: Char)

        fun end(state: State)

        fun tryEnd(state: State)

        class Format : Parser {
            companion object {
                const val STATE_NAME = 0
                const val STATE_NAMING = 1
                const val STATE_BEGIN_CONTENT = 2
                const val STATE_END_CONTENT = 3
                const val STATE_BEGIN_OPTIONS = 4
                const val STATE_END_OPTIONS = 5
            }

            val type = StringBuilder()
            var options: MutableList<ParseResult>? = null
            var results: MutableList<ParseResult>? = null
            var state = STATE_NAME

            val optionsSafe: MutableList<ParseResult>
                get() = options ?: ArrayList<ParseResult>().apply { options = this }
            val resultsSafe: MutableList<ParseResult>
                get() = results ?: ArrayList<ParseResult>().apply { results = this }

            override fun parse(state: State, char: Char) {
                when (this.state) {
                    STATE_NAME -> when (char) {
                        BEGIN_FORMAT -> throw PatternException("Can't begin format during format name")
                        BEGIN_FORMAT_CONTENT -> throw PatternException("Can't begin format content without naming the format")
                        END_FORMAT_CONTENT -> throw PatternException("Can't end format content without naming the format")
                        else -> {
                            type.append(char)
                            this.state = STATE_NAMING
                        }
                    }

                    STATE_NAMING -> when (char) {
                        BEGIN_FORMAT -> { // Beginning another format, or literal text
                            end(state)
                            state.pushParser(Text())
                            state.parserState.parse(state, char)
                        }

                        BEGIN_FORMAT_CONTENT -> {
                            this.state = STATE_BEGIN_CONTENT
                            resultsSafe
                            state.pushAppender { resultsSafe.add(it) }
                            state.pushParser(Text())
                        }

                        END_FORMAT_CONTENT -> { // Simple format, no content, no options. End next format content
                            end(state)
                            state.parserState.parse(state, char)
                        }

                        END_FORMAT -> {
                            end(state)
                            state.parserState.parse(state, char)
                        }

                        else -> {
                            if ((char < 'a' || char > 'z') && char != '_') { // Format name terminated by invalid character
                                end(state)
                                state.parserState.parse(state, char)
                            } else type.append(char)
                        }
                    }

                    STATE_BEGIN_CONTENT -> when (char) {
                        BEGIN_FORMAT -> throw PatternException("Pattern error: Shouldn't be possible")
                        BEGIN_FORMAT_CONTENT -> throw PatternException("Pattern error: Shouldn't be possible")
                        END_FORMAT_CONTENT -> {
                            this.state = STATE_END_CONTENT
                            state.popAppender
                        }
                    }

                    STATE_END_CONTENT -> when (char) {
                        BEGIN_FORMAT -> { // Beginning another format, or literal text
                            end(state)
                            state.pushParser(Text())
                            state.parserState.parse(state, char)
                        }

                        BEGIN_FORMAT_CONTENT -> {
                            this.state = STATE_BEGIN_OPTIONS
                            optionsSafe
                            state.pushAppender { optionsSafe.add(it) }
                            state.pushParser(Text())
                        }

                        END_FORMAT_CONTENT -> { // Format with content but no options. End next format content
                            end(state)
                            state.parserState.parse(state, char)
                        }

                        else -> { // Keep on typing illegal character - probably text
                            end(state)
                            state.parserState.parse(state, char)
                        }
                    }

                    STATE_BEGIN_OPTIONS -> when (char) {
                        BEGIN_FORMAT -> throw PatternException("Pattern error: Shouldn't be possible")
                        BEGIN_FORMAT_CONTENT -> throw PatternException("Pattern error: Shouldn't be possible")
                        END_FORMAT_CONTENT -> {
                            this.state = STATE_END_OPTIONS
                            state.popAppender
                            end(state)
                        }
                    }

                    STATE_END_OPTIONS -> throw PatternException("Pattern error: Shouldn't be possible")
                }
            }

            override fun end(state: State) {
                val p = state.popParser
                if (p != this) throw PatternException("State parser mismatch: Expected Format, found ${p::class.simpleName}")
                val type = PatternRegistry.pattern(this.type.toString())
                state.pushParser(Text())
                state.appender(ParseResult.Formatted(type, results?.build, options?.build))
            }

            override fun tryEnd(state: State) {
                if (this.state == STATE_NAMING || this.state == STATE_END_CONTENT) {
                    end(state)
                } else throw PatternException("Invalid state for tryEnd: ${this.state}")
            }
        }

        class Text : Parser {
            companion object {
                const val STATE_TEXT = 0
                const val STATE_FORMAT = 1
            }

            val text = StringBuilder()
            var state = STATE_TEXT
            override fun parse(state: State, char: Char) {
                when (this.state) {
                    STATE_TEXT -> when (char) {
                        BEGIN_FORMAT -> this.state = STATE_FORMAT
                        END_FORMAT -> throw PatternException("End format ($END_FORMAT) is not allowed here without escaping")
                        BEGIN_FORMAT_CONTENT -> throw PatternException("Begin format ($BEGIN_FORMAT_CONTENT) not allowed here without escaping")
                        END_FORMAT_CONTENT -> {
                            end(state)
                            state.parserState.parse(state, char)
                        }

                        else -> text.append(char)
                    }

                    STATE_FORMAT -> when (char) {
                        BEGIN_FORMAT, END_FORMAT, BEGIN_FORMAT_CONTENT, END_FORMAT_CONTENT -> {
                            text.append(char)
                            this.state = STATE_TEXT
                        }

                        else -> {
                            end(state, Format())
                            state.parserState.parse(state, char)
                        }
                    }
                }
            }

            override fun end(state: State) {
                end(state, null)
            }

            override fun tryEnd(state: State) {
                if (this.state == STATE_FORMAT) throw PatternException("Can't end in format")
                end(state)
            }

            fun end(state: State, newParser: Parser?) {
                val p = state.popParser
                if (p != this) throw PatternException("State parser mismatch: Expected Text, found ${p::class.simpleName}")
                if (newParser != null) state.pushParser(newParser)
                if (text.isEmpty()) return
                state.appender(ParseResult.Text(text.toString()))
            }
        }
    }

    private class State {
        val results = ArrayList<ParseResult>()
        var parserStateUnsafe: Parser? = null
        val parserState: Parser
            get() {
                if (parserStateUnsafe == null) {
                    Thread.dumpStack()
                    pushParser(Parser.Text())
                }
                return parserStateUnsafe!!
            }
        val parserStates: MutableList<Parser> = ArrayList()
        var appender: (ParseResult) -> Unit = { results.add(it) }
        val appenders: MutableList<(ParseResult) -> Unit> = arrayListOf(appender)
        var cursor = 0

        val popParser: Parser
            get() {
                val parser = parserStates.removeLast()
                parserStateUnsafe = parserStates.lastOrNull()
                return parser
            }

        fun pushParser(parser: Parser) {
            parserStates.addLast(parser)
            parserStateUnsafe = parser
        }

        val popAppender: (ParseResult) -> Unit
            get() {
                if (appenders.size == 1) throw PatternException("Invalid pattern format")
                val appender = appenders.removeLast()
                this.appender = appenders.last()
                return appender
            }

        fun pushAppender(appender: (ParseResult) -> Unit) {
            appenders.addLast(appender)
            this.appender = appender
        }
    }
}

private fun StringBuilder.indent(level: Int): StringBuilder {
    return repeat("  ", level)
}

/**
 * Syntax works as follows:
 * ```
 * message = "msg"
 * thread = "main"
 * lsb = "%style{[}{#646464}"
 * rsb = "%style{]}{#646464}"
 * sb{content} = "%style{content}{#646464}
 *
 * "%message" -> "msg"
 *
 * "%lsb%thread%rsb: %message" -> "[main]: msg"
 * ```
 */
sealed interface ParseResult {
    val build: String

    fun simplify(): ParseResult

    fun printTree(builder: StringBuilder, level: Int = 0)

    val simplified: Boolean


    data object Empty : ParseResult {
        override val build: String = ""
        override fun simplify(): ParseResult = this
        override fun printTree(builder: StringBuilder, level: Int) {
            builder.append("[]")
        }

        override val simplified: Boolean = true
        override fun toString(): String = ""
    }

    data class Formatted(
        val pattern: CustomPattern, val content: ParseResult? = null, val options: ParseResult? = null
    ) : ParseResult {

        constructor(
            pattern: String, options: ParseResult? = null, content: ParseResult? = null
        ) : this(PatternRegistry.pattern(pattern), options, content)

        init {
            if (options != null && content == null) {
                throw PatternException("Bad pattern input: Options can't be nonnull with content null")
            }
        }

        fun assertContentEmpty() {
            if (content != null && content !is Empty) throw PatternException("Content for ${pattern.name} must be null or empty")
        }

        fun assertContentNull() {
            if (content != null) throw PatternException("Content for ${pattern.name} must be null")
        }

        fun assertOptionsNull() {
            if (options != null) throw PatternException("Options for ${pattern.name} must be null")
        }

        fun assertContentNotNull(): ParseResult {
            if (content == null) throw PatternException("Content for ${pattern.name} must not be null")
            return content
        }

        fun assertOptionsNotNull(): ParseResult {
            if (options == null) throw PatternException("Options for ${pattern.name} must not be null")
            return options
        }

        override fun simplify(): ParseResult {
            if (simplified) return this
            val maxDepth = 500
            var depth = 0
            var simple: ParseResult = pattern.simplifier.simplify(pattern, this)
            while (!simple.simplified) {
                depth++
                simple = simple.simplify()
                if (depth > maxDepth) {
                    val b = StringBuilder()
                    simple.printTree(b)
                    throw PatternException("Failed to simplify with depth 500: \n$b")
                }
            }
            return simple
        }

        override fun printTree(builder: StringBuilder, level: Int) {
            builder.append("%${pattern.name}")
            if (content != null) {
                builder.appendLine()
                builder.indent(level + 1).append("content=")
                content.printTree(builder, level + 1)
                if (options != null) {
                    builder.appendLine()
                    builder.indent(level + 1).append("options=")
                    options.printTree(builder, level + 1)
                }
            }
        }

        override val simplified: Boolean =
            pattern.simplifier == CustomPattern.NativeSimplifier && content?.simplified ?: true && options?.simplified ?: true

        override val build: String =
            "%${pattern.name}${content?.build?.let { it -> "{$it}${options?.build?.let { "{$it}" } ?: ""}" } ?: ""}"
    }

    data class Text(val text: String) : ParseResult {
        override val build: String = text
        override fun simplify(): ParseResult = this
        override fun printTree(builder: StringBuilder, level: Int) {
            builder.append('"').append(text).append('"')
        }

        override val simplified: Boolean = true
    }

    data class Multi(val results: List<ParseResult>) : ParseResult {
        constructor(vararg results: ParseResult) : this(results.toList())

        override val build: String = results.joinToString("", transform = { it.build })
        override fun simplify(): ParseResult = Multi(results.map { it.simplify() })
        override fun printTree(builder: StringBuilder, level: Int) {
            builder.appendLine("[")
            results.forEach {
                builder.indent(level + 1)
                it.printTree(builder, level + 1)
                builder.appendLine()
            }
            builder.indent(level).append("]")
        }

        override val simplified: Boolean = results.all { it.simplified }
    }
}

val Color.styleHex: String
    get() = "#$rgbHex"

object Logging {
    val `in` = System.`in`!!
    val out = System.out!!
    val err = System.err!!
}

@Suppress("MemberVisibilityCanBePrivate", "unused")
object CustomPatterns {
    val C_TRACE = Color(255, 0, 255).styleHex
    val C_DEBUG = Color(150, 150, 150).styleHex
    val C_INFO = Color(234, 218, 228).styleHex
    val C_WARN = Color(255, 255, 0).styleHex
    val C_ERROR = Color(150, 0, 0).styleHex
    val C_FATAL = Color(100, 0, 0).styleHex
    val C_STDOUT = Color(170, 170, 170).styleHex
    val C_STDERR = Color(180, 0, 0).styleHex
    val C_THREAD = Color(200, 200, 200).styleHex
    val C_LOGGER = Color(0, 100, 255).styleHex
    val C_LOCATION = Color(150, 150, 150).styleHex
    val C_TIME = Color(70, 255, 70).styleHex
    val C_GRAY = Color(100, 100, 100).styleHex

    val STYLE = PatternRegistry.pattern("style")
    val TIME = PatternRegistry.pattern("time")
    val LEVEL = PatternRegistry.pattern("level")
    val LOGGER = PatternRegistry.pattern("logger")
    val THREAD = PatternRegistry.pattern("thread")
    val HIGHLIGHT = PatternRegistry.pattern("highlight")
    val EXCEPTION = PatternRegistry.pattern("exception")
    val MARKER = PatternRegistry.pattern("marker")
    val GRAY = PatternRegistry.pattern("gray")
    val SB = PatternRegistry.pattern("sb")
    val MSG = PatternRegistry.pattern("msg")
    val LSB = PatternRegistry.pattern("lsb")
    val RSB = PatternRegistry.pattern("rsb")
    val LOCATION = PatternRegistry.pattern("location")
    val NEWLINE = PatternRegistry.pattern("n")
    val N_MSG = PatternRegistry.pattern("n_msg")
    val N_HIGHLIGHT = PatternRegistry.pattern("n_highlight")
    val N_TIME = PatternRegistry.pattern("n_time")
    val N_LEVEL = PatternRegistry.pattern("n_level")
    val N_LOGGER = PatternRegistry.pattern("n_logger")
    val N_THREAD = PatternRegistry.pattern("n_thread")
    val N_EXCEPTION = PatternRegistry.pattern("n_exception")
    val N_MARKER = PatternRegistry.pattern("n_marker")
    val N_LOCATION = PatternRegistry.pattern("n_location")
    val DEFAULT_PATTERN = "$TIME $LEVEL $LOGGER $THREAD: $MSG$NEWLINE$EXCEPTION"
    val DEFAULT_CUSTOM_PATTERN = "$TIME $LEVEL $MARKER $THREAD: $MSG$NEWLINE$EXCEPTION"
    val DEFAULT_STDOUT_PATTERN =
        "$TIME $SB{$STYLE{STDOUT}{$C_STDOUT}} $THREAD $LOCATION $STYLE{$N_MSG}{$C_STDOUT}$NEWLINE$EXCEPTION"
    val DEFAULT_STDERR_PATTERN =
        "$TIME $SB{$STYLE{STDERR}{$C_STDERR}} $THREAD $LOCATION $STYLE{$N_MSG}{$C_STDERR}$NEWLINE$EXCEPTION"

    fun pattern(level: LogLevel): String {
        return when (level) {
            LogLevel.Root -> DEFAULT_PATTERN
            LogLevel.Stdout -> DEFAULT_STDOUT_PATTERN
            LogLevel.Stderr -> DEFAULT_STDERR_PATTERN
            is LogLevel.Pattern -> level.pattern
            is LogLevel.Custom -> DEFAULT_CUSTOM_PATTERN
            is LogLevel.Colored -> DEFAULT_CUSTOM_PATTERN.run {
                val hasStyle = level.fgStyle != null || level.bgStyle != null
                if (!hasStyle) return this
                val style = StringBuilder()
                if (level.fgStyle != null) {
                    style.append(level.fgStyle)
                }
                if (level.bgStyle != null) {
                    if (style.isNotEmpty()) style.append(' ')
                    style.append("bg_")
                    style.append(level.bgStyle)
                }
                replace("$MARKER", "$MARKER{$style}").replace("$MSG", "$MSG{$style}")
            }
        }
    }
}

class CustomPattern(
    val name: String, val simplifier: Simplifier
) {
    fun interface Simplifier {
        fun simplify(pattern: CustomPattern, parse: ParseResult.Formatted): ParseResult
    }

    object NativeSimplifier : Simplifier {
        override fun simplify(pattern: CustomPattern, parse: ParseResult.Formatted): ParseResult {
            if (parse.simplified) return parse
            return ParseResult.Formatted(parse.pattern, parse.content?.simplify(), parse.options?.simplify())
        }
    }

    override fun toString(): String = "%$name"

    interface PlatformProvider {
        fun pattern(name: String): CustomPattern

        fun isNative(name: String): Boolean

        fun freeze() {
            PatternRegistry.freeze()
        }
    }
}
