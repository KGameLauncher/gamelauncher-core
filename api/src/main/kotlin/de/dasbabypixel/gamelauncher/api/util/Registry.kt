package de.dasbabypixel.gamelauncher.api.util

import java.util.concurrent.ConcurrentHashMap

object Registry {
    private val map: MutableMap<Class<*>, Any> = ConcurrentHashMap()

    fun <T : Any> register(cls: Class<T>, value: T) {
        map[cls] = value
    }

    inline fun <reified T : Any> register(value: T) {
        register(T::class.java, value)
    }

    fun <T : Any> instance(cls: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return map[cls]!! as T
    }

    inline fun <reified T : Any> instance(): T {
        return instance(T::class.java)
    }
}