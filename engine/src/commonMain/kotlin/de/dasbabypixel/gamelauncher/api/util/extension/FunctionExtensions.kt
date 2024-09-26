package de.dasbabypixel.gamelauncher.api.util.extension

import de.dasbabypixel.gamelauncher.api.util.function.*
import java.util.concurrent.Callable
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Supplier

fun Runnable.toGameRunnable(): GameRunnable = runnable(this) { run() }
fun <T> Callable<T>.toGameCallable(): GameCallable<T> = callable(this) { call() }
fun <T> Consumer<T>.toGameConsumer(): GameConsumer<T> = consumer(this) { accept(it) }
fun <T> Supplier<T>.toGameSupplier(): GameSupplier<T> = supplier(this) { get() }
fun <T, V> Function<T, V>.toGameFunction(): GameFunction<T, V> = function(this) { apply(it) }

fun <T> Function0<T>.toGameSupplier(): GameSupplier<T> = supplier(this) { invoke() }
fun <T> Function0<T>.toGameCallable(): GameCallable<T> = callable(this) { invoke() }
fun <T, V> Function1<T, V>.toGameFunction(): GameFunction<T, V> = function(this) { invoke(it) }

fun <T> GameCallable<T>.toRunnable(): GameRunnable = runnable(this) { call() }

fun GameRunnable.toCallable(): GameCallable<Unit> = callable(this) { run() }

private inline fun <T> consumer(o: Any, crossinline task: (T) -> Unit): GameConsumer<T> {
    return object : GameConsumer<T> {
        override fun toString(): String {
            return o.toString()
        }

        override fun accept(value: T) {
            task(value)
        }
    }
}

private inline fun <T> supplier(o: Any, crossinline task: () -> T): GameSupplier<T> {
    return object : GameSupplier<T> {
        override fun toString(): String {
            return o.toString()
        }

        override fun get(): T {
            return task()
        }
    }
}

private inline fun <T, V> function(o: Any, crossinline task: (t: T) -> V): GameFunction<T, V> {
    return object : GameFunction<T, V> {
        override fun toString(): String {
            return o.toString()
        }

        override fun apply(value: T): V {
            return task(value)
        }
    }
}

private inline fun runnable(o: Any, crossinline task: () -> Unit): GameRunnable {
    return object : GameRunnable {
        override fun toString(): String {
            return o.toString()
        }

        override fun run() {
            task()
        }
    }
}

private inline fun <T> callable(o: Any, crossinline task: () -> T): GameCallable<T> {
    return object : GameCallable<T> {
        override fun toString(): String {
            return o.toString()
        }

        override fun call(): T {
            return task()
        }
    }
}