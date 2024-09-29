package de.dasbabypixel.gamelauncher.api.util.concurrent

import de.dasbabypixel.gamelauncher.api.util.extension.toCallable
import de.dasbabypixel.gamelauncher.api.util.extension.toGameCallable
import de.dasbabypixel.gamelauncher.api.util.extension.toGameRunnable
import de.dasbabypixel.gamelauncher.api.util.function.GameCallable
import de.dasbabypixel.gamelauncher.api.util.function.GameRunnable
import de.dasbabypixel.gamelauncher.api.util.resource.GameResource
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture

expect sealed interface ThreadGroup {
    val name: String
    val parent: ThreadGroup?

    companion object {
        fun create(name: String): ThreadGroup
        fun create(name: String, parent: ThreadGroup): ThreadGroup
    }
}

expect fun currentThread(): Thread
fun sleep(millis: Long) = ThreadMethods.sleep(millis)

interface Thread : ThreadHolder, GameResource {
    companion object {
        fun park() {
            ThreadMethods.park()
        }

        fun park(nanos: Long) {
            ThreadMethods.park(nanos)
        }
    }

    val name: String
    val group: ThreadGroup
    val stackTrace: Array<StackTraceElement>
    override val thread: Thread
        get() = this

    fun start()
    fun unpark()

    fun ensureOnThread() {
        val thread = currentThread()
        if (thread != this) {
            throw IllegalStateException("Wrong thread! Expected $name, was ${thread.name}")
        }
    }
}

expect object ThreadMethods {
    fun park()
    fun park(nanos: Long)
    fun sleep(millis: Long)
}

interface ThreadHolder {
    val thread: Thread
}

interface ExecutorThread : Thread, Executor

interface Executor {
    fun submit(runnable: Runnable): CompletableFuture<Unit> = submit(runnable.toGameRunnable())
    fun submit(runnable: GameRunnable): CompletableFuture<Unit> = submit(runnable.toCallable())

    fun <T> submit(callable: Callable<T>): CompletableFuture<T> = submit(callable.toGameCallable())
    fun <T> submit(callable: GameCallable<T>): CompletableFuture<T>
}
