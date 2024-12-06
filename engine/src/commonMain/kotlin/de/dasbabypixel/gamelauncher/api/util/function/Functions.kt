package de.dasbabypixel.gamelauncher.api.util.function

import de.dasbabypixel.gamelauncher.api.util.GameException

fun interface GameRunnable {
    @Throws(GameException::class)
    fun run()
}

fun interface GameCallable<T> {
    @Throws(GameException::class)
    fun call(): T
}

fun interface GameConsumer<T> {
    @Throws(GameException::class)
    fun accept(value: T)
}

fun interface GameBiConsumer<T, V> {
    @Throws(GameException::class)
    fun accept(t: T, v: V)
}

fun interface GameSupplier<T> {
    @Throws(GameException::class)
    fun get(): T
}

fun interface GameFunction<T, V> {
    @Throws(GameException::class)
    fun apply(value: T): V
}
