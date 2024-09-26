package de.dasbabypixel.gamelauncher.lwjgl.util.logging

import de.dasbabypixel.gamelauncher.api.util.Registry
import de.dasbabypixel.gamelauncher.api.util.logging.CustomPatterns.C_DEBUG
import de.dasbabypixel.gamelauncher.api.util.logging.CustomPatterns.C_ERROR
import de.dasbabypixel.gamelauncher.api.util.logging.CustomPatterns.C_FATAL
import de.dasbabypixel.gamelauncher.api.util.logging.CustomPatterns.C_INFO
import de.dasbabypixel.gamelauncher.api.util.logging.CustomPatterns.C_TRACE
import de.dasbabypixel.gamelauncher.api.util.logging.CustomPatterns.C_WARN
import de.dasbabypixel.gamelauncher.api.util.logging.Logging
import de.dasbabypixel.gamelauncher.api.util.logging.ParseResult
import de.dasbabypixel.gamelauncher.api.util.logging.PatternException
import de.dasbabypixel.gamelauncher.common.util.logging.CommonPatternProvider

object Log4jPatternSerializer {
    private val serializers = HashMap<String, Serializer>()

    init {
        serializers["n_time"] = Serializer.Time
        serializers["n_level"] = Serializer.Level
        serializers["n_logger"] = Serializer.Logger
        serializers["n_thread"] = Serializer.Thread
        serializers["n_highlight"] = Serializer.Highlight
        serializers["n_exception"] = Serializer.Exception
        serializers["n_marker"] = Serializer.Marker
        serializers["n_location"] = Serializer.Location
        serializers["n_msg"] = Serializer.Msg
        serializers["n"] = Serializer.Newline
        serializers["style"] = Serializer.Style

        var missing = false
        Registry.instance<CommonPatternProvider>().patternNames.forEach {
            if (!serializers.containsKey(it)) {
                Logging.out.println("Missing pattern $it")
                missing = true
            }
        }
        if (missing) throw PatternException("Missing patterns in platform")
    }

    fun serialize(parse: ParseResult): String {
        val state = State()
        state.serialize(if (parse.simplified) parse else parse.simplify())
        if (state.textBuilder.isNotEmpty()) {
            state.builder.append(state.textBuilder)
        }
        return state.build()
    }

    private class State {
        private val styles = ArrayList<Styles>()
        val builder = StringBuilder()
        val textBuilder = StringBuilder()
        fun build() = builder.toString()

        fun serialize(parse: ParseResult) {
            when (parse) {
                ParseResult.Empty -> {}
                is ParseResult.Text -> addStyleable(parse.text)
                is ParseResult.Formatted -> {
                    serializers[parse.pattern.name]!!.serialize(this, parse)
                }

                is ParseResult.Multi -> {
                    for (result in parse.results) {
                        serialize(result)
                    }
                }
            }
        }

        fun pushStyle(style: Styles) {
            if (textBuilder.isNotEmpty()) {
                if (styles.isNotEmpty()) {
                    val s = styles.last()
                    builder.append(s.prefix)
                    builder.append(textBuilder)
                    builder.append(s.suffix)
                } else {
                    builder.append(textBuilder)
                }
                textBuilder.clear()
            }
            styles.addLast(style)
        }

        fun popStyle() {
            if (textBuilder.isNotEmpty()) {
                val s = styles.last()
                builder.append(s.prefix)
                builder.append(textBuilder)
                textBuilder.clear()
                builder.append(s.suffix)
            }
            styles.removeLast()
        }

        fun addStyleable(format: String) {
            textBuilder.append(format)
        }
    }

    private sealed interface Styles {
        val prefix: String
        val suffix: String

        data class Style(val options: String) : Styles {
            override val prefix: String = "%style{"
            override val suffix: String = "}{$options}"
        }

        data class Highlight(val options: String) : Styles {
            override val prefix: String = "%highlight{"
            override val suffix: String = "}{$options}"
        }
    }

    private sealed interface Serializer {
        fun serialize(state: State, format: ParseResult.Formatted)

        data object Location : Serializer {
            override fun serialize(state: State, format: ParseResult.Formatted) {
                format.assertContentNull()
                format.assertOptionsNull()
                state.addStyleable("%location")
            }
        }

        data object Marker : Serializer {
            override fun serialize(state: State, format: ParseResult.Formatted) {
                format.assertContentNull()
                format.assertOptionsNull()
                state.addStyleable("%marker")
            }
        }

        data object Exception : Serializer {
            override fun serialize(state: State, format: ParseResult.Formatted) {
                format.assertContentNull()
                format.assertOptionsNull()
                state.addStyleable("%ex")
            }
        }

        data object Newline : Serializer {
            override fun serialize(state: State, format: ParseResult.Formatted) {
                format.assertContentNull()
                format.assertOptionsNull()
                state.addStyleable("%n")
            }
        }

        data object Time : Serializer {
            override fun serialize(state: State, format: ParseResult.Formatted) {
                format.assertContentNull()
                format.assertOptionsNull()
                state.addStyleable("%d{ABSOLUTE}")
            }
        }

        data object Level : Serializer {
            override fun serialize(state: State, format: ParseResult.Formatted) {
                format.assertContentNull()
                format.assertOptionsNull()
                state.addStyleable("%p{length=1}")
            }
        }

        data object Logger : Serializer {
            override fun serialize(state: State, format: ParseResult.Formatted) {
                format.assertContentNull()
                format.assertOptionsNull()
                state.addStyleable("%c{1.}")
            }
        }

        data object Thread : Serializer {
            override fun serialize(state: State, format: ParseResult.Formatted) {
                format.assertContentNull()
                format.assertOptionsNull()
                state.addStyleable("%t")
            }
        }

        data object Msg : Serializer {
            override fun serialize(state: State, format: ParseResult.Formatted) {
                format.assertContentNull()
                format.assertOptionsNull()
                state.addStyleable("%m")
            }
        }

        data object Highlight : Serializer {
            private val format =
                "FATAL=$C_FATAL, ERROR=$C_ERROR, WARN=$C_WARN, INFO=$C_INFO, DEBUG=$C_DEBUG, TRACE=$C_TRACE"

            override fun serialize(state: State, format: ParseResult.Formatted) {
                val content = format.assertContentNotNull()
                format.assertOptionsNull()
                state.pushStyle(Styles.Highlight(Highlight.format))
                state.serialize(content)
                state.popStyle()
            }
        }

        data object Style : Serializer {
            override fun serialize(state: State, format: ParseResult.Formatted) {
                val content = format.assertContentNotNull()
                val options = format.assertOptionsNotNull()
                if (options !is ParseResult.Text) throw PatternException("Invalid content in options for style")
                state.pushStyle(Styles.Style(options.text))
                state.serialize(content)
                state.popStyle()
            }
        }
    }
}