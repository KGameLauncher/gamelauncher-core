package de.dasbabypixel.gamelauncher.api.config

import java.util.concurrent.ConcurrentHashMap

object Config {
    private val config = ConcurrentHashMap<String, ConfigValue<out Any>>()

    val NAME = createString("name", "GameLauncher")
    val IN_IDE = createBoolean("in_ide", false)
    val USE_ANSI = createBoolean("use_ansi", IN_IDE.value || System.console() != null)
    val DEBUG = createBoolean("debug", IN_IDE)
    val TRACK_RESOURCES = createBoolean("track_resources", DEBUG)
    val CALCULATE_THREAD_STACKS = createBoolean("calculate_thread_stacks", DEBUG)

    fun <T : Any> named(name: String): ConfigValue<T> {
        @Suppress("UNCHECKED_CAST") return (config[name]
            ?: throw IllegalStateException("No config with name $name registered")) as ConfigValue<T>
    }

    fun <T : Any> create(name: String, defaultValue: T): ConfigValue<T> {
        return create(name, defaultValue, defaultValue)
    }

    fun <T : Any> create(name: String, defaultValue: ConfigValue<T>): ConfigValue<T> {
        return create(name, defaultValue.value)
    }

    fun createString(name: String, defaultValue: String): ConfigValue<String> {
        return create(name, defaultValue, systemProperty(name))
    }

    fun createString(name: String, defaultValue: ConfigValue<String>): ConfigValue<String> {
        return createString(name, defaultValue.value)
    }

    fun createBoolean(name: String, defaultValue: Boolean): ConfigValue<Boolean> {
        return create(name, defaultValue, systemPropertyBoolean(name))
    }

    fun createBoolean(name: String, defaultValue: ConfigValue<Boolean>): ConfigValue<Boolean> {
        return createBoolean(name, defaultValue.value)
    }

    fun createInt(name: String, defaultValue: Int): ConfigValue<Int> {
        return create(name, defaultValue, systemPropertyInt(name))
    }

    fun createInt(name: String, defaultValue: ConfigValue<Int>): ConfigValue<Int> {
        return create(name, defaultValue.value)
    }

    private fun <T : Any> create(name: String, defaultValue: T, value: T?): ConfigValue<T> {
        val c = ConfigValue(name, defaultValue, value ?: defaultValue)
        if (config.putIfAbsent(name, c) != null) {
            throw IllegalStateException("Config with name $name already registered")
        }
        return c
    }

    private fun systemProperty(name: String): String? {
        return System.getProperty("gamelauncher.$name")
    }

    private fun systemPropertyInt(name: String): Int? {
        val prop = systemProperty(name)
        return prop?.toIntOrNull()
    }

    private fun systemPropertyBoolean(name: String): Boolean? {
        val prop = systemProperty(name)
        if (prop == "true" || prop == "1") return true
        if (prop == "false" || prop == "0") return false
        return null
    }

    class ConfigValue<T>(val name: String, val defaultValue: T, value: T) {
        var value: T = value
            private set

        init {
            if (name.lowercase() != name) throw IllegalArgumentException("Name must be namespaced lowercase")
        }

        fun reset() {
            value = defaultValue
        }
    }
}
