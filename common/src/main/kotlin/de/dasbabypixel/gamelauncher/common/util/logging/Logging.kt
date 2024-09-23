package de.dasbabypixel.gamelauncher.common.util.logging

import de.dasbabypixel.gamelauncher.api.util.Registry
import de.dasbabypixel.gamelauncher.api.util.logging.*
import de.dasbabypixel.gamelauncher.api.util.logging.CustomPatterns.C_GRAY
import de.dasbabypixel.gamelauncher.api.util.logging.CustomPatterns.C_LOCATION
import de.dasbabypixel.gamelauncher.api.util.logging.CustomPatterns.C_LOGGER
import de.dasbabypixel.gamelauncher.api.util.logging.CustomPatterns.C_THREAD
import de.dasbabypixel.gamelauncher.api.util.logging.CustomPatterns.C_TIME
import de.dasbabypixel.gamelauncher.api.util.logging.CustomPatterns.HIGHLIGHT
import de.dasbabypixel.gamelauncher.api.util.logging.CustomPatterns.N_EXCEPTION
import de.dasbabypixel.gamelauncher.api.util.logging.CustomPatterns.N_HIGHLIGHT
import de.dasbabypixel.gamelauncher.api.util.logging.CustomPatterns.N_LEVEL
import de.dasbabypixel.gamelauncher.api.util.logging.CustomPatterns.N_LOCATION
import de.dasbabypixel.gamelauncher.api.util.logging.CustomPatterns.N_LOGGER
import de.dasbabypixel.gamelauncher.api.util.logging.CustomPatterns.N_MARKER
import de.dasbabypixel.gamelauncher.api.util.logging.CustomPatterns.N_MSG
import de.dasbabypixel.gamelauncher.api.util.logging.CustomPatterns.N_THREAD
import de.dasbabypixel.gamelauncher.api.util.logging.CustomPatterns.N_TIME
import de.dasbabypixel.gamelauncher.api.util.logging.CustomPatterns.SB
import de.dasbabypixel.gamelauncher.api.util.logging.CustomPatterns.STYLE
import de.dasbabypixel.gamelauncher.api.util.logging.ParseResult.*

@Suppress("MemberVisibilityCanBePrivate")
object CommonPatternProvider : CustomPattern.PlatformProvider {
    init {
        Registry.register(CommonPatternProvider)
        Registry.register<CustomPattern.PlatformProvider>(CommonPatternProvider)
    }

    private val patterns: MutableMap<String, CustomPattern> = HashMap()


    fun addPattern(name: String) {
        val pattern = CustomPattern(name, CustomPattern.NativeSimplifier)
        patterns[name] = pattern
        PatternRegistry.registerPattern(pattern)
    }

    fun register() {
        addPattern("style")
        addPattern("n_msg")
        addPattern("n_highlight")
        addPattern("n_time")
        addPattern("n_level")
        addPattern("n_logger")
        addPattern("n_thread")
        addPattern("n_exception")
        addPattern("n_marker")
        addPattern("n_location")
        addPattern("n")

        PatternRegistry.registerPattern(CustomPattern("location") { _, parse ->
            parse.assertContentNull()
            parse.assertOptionsNull()
            Formatted(SB, Formatted(STYLE, Formatted(N_LOCATION), Text(C_LOCATION)))
        })
        PatternRegistry.registerPattern(CustomPattern("msg") { _, parse ->
            parse.assertOptionsNull()
            val style = parse.content
            if (style != null) {
                Formatted(STYLE, Formatted(N_MSG), style)
            } else {
                Formatted(HIGHLIGHT, Formatted(N_MSG))
            }
        })
        PatternRegistry.registerPattern(CustomPattern("marker") { _, parse ->
            parse.assertOptionsNull()
            val style = parse.content ?: Text(C_LOGGER)
            Formatted(SB, Formatted(STYLE, Formatted(N_MARKER), style))
        })
        PatternRegistry.registerPattern(CustomPattern("exception") { _, parse ->
            parse.assertContentNull()
            parse.assertOptionsNull()
            Formatted(HIGHLIGHT, Formatted(N_EXCEPTION))
        })
        PatternRegistry.registerPattern(CustomPattern("highlight") { _, parse ->
            parse.assertContentNotNull()
            parse.assertOptionsNull()
            Formatted(N_HIGHLIGHT, parse.content)
        })
        PatternRegistry.registerPattern(CustomPattern("time") { _, parse ->
            parse.assertContentNull()
            parse.assertOptionsNull()
            Formatted(SB, Formatted(STYLE, Formatted(N_TIME), Text(C_TIME)))
        })
        PatternRegistry.registerPattern(CustomPattern("level") { _, parse ->
            parse.assertContentNull()
            parse.assertOptionsNull()
            Formatted(SB, Formatted(HIGHLIGHT, Formatted(N_LEVEL)))
        })
        PatternRegistry.registerPattern(CustomPattern("logger") { _, parse ->
            parse.assertContentNull()
            parse.assertOptionsNull()
            Formatted(SB, Formatted(STYLE, Formatted(N_LOGGER), Text(C_LOGGER)))
        })
        PatternRegistry.registerPattern(CustomPattern("thread") { _, parse ->
            parse.assertContentNull()
            parse.assertOptionsNull()
            Formatted(SB, Formatted(STYLE, Formatted(N_THREAD), Text(C_THREAD)))
        })
        PatternRegistry.registerPattern(CustomPattern("gray") { _, parse ->
            parse.assertContentNotNull()
            parse.assertOptionsNull()
            Formatted(STYLE, parse.content, Text(C_GRAY))
        })
        PatternRegistry.registerPattern(CustomPattern("sb") { _, parse ->
            parse.assertContentNotNull()
            parse.assertOptionsNull()
            Formatted(CustomPatterns.GRAY, Multi(Text("["), parse.content ?: Empty, Text("]")))
        })
        PatternRegistry.registerPattern(CustomPattern("lsb") { _, parse ->
            parse.assertContentEmpty()
            Formatted(STYLE, Text("["), parse.options ?: Text(C_GRAY))
        })
        PatternRegistry.registerPattern(CustomPattern("rsb") { _, parse ->
            parse.assertContentEmpty()
            Formatted(STYLE, Text("]"), parse.options ?: Text(C_GRAY))
        })
    }

    val patternNames: List<String> = this.patterns.keys.toList()

    override fun pattern(name: String): CustomPattern {
        return patterns[name] ?: throw PatternException("No native pattern named $name")
    }

    override fun isNative(name: String): Boolean {
        return patterns.containsKey(name)
    }
}
