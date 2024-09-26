package de.dasbabypixel.gamelauncher.api.util.concurrent

import com.lmax.disruptor.RingBuffer
import com.lmax.disruptor.SleepingWaitStrategy
import de.dasbabypixel.gamelauncher.api.util.Debug
import de.dasbabypixel.gamelauncher.api.util.GameException
import de.dasbabypixel.gamelauncher.api.util.function.GameCallable
import de.dasbabypixel.gamelauncher.api.util.logging.getLogger
import de.dasbabypixel.gamelauncher.api.util.resource.AbstractGameResource
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean

expect abstract class AbstractThread : AbstractGameResource, Thread {
    constructor(group: ThreadGroup)
    constructor(group: ThreadGroup, daemon: Boolean)
    constructor(group: ThreadGroup, name: String)
    constructor(group: ThreadGroup, name: String, daemon: Boolean)

    final override val stackTrace: Array<StackTraceElement>
    final override val name: String
    final override val group: ThreadGroup
    override val autoTrack: Boolean

    fun start()
    final override fun unpark()
    protected abstract fun run()
}

abstract class AbstractExecutorThread : AbstractThread, ExecutorThread, StackTraceSnapshot.CauseContainer {
    companion object {
        val logger = getLogger<AbstractExecutorThread>()
    }

    final override var cause: StackTraceSnapshot? = null
    private val ringBuffer: RingBuffer<QueueEntry<out Any>> =
        RingBuffer.createMultiProducer(::QueueEntry, 1024, SleepingWaitStrategy())
    private val poller = ringBuffer.newPoller()
    protected val exit = AtomicBoolean(false)
    private val queueFinalized = AtomicBoolean(false)

    constructor(group: ThreadGroup) : super(group)
    constructor(group: ThreadGroup, daemon: Boolean) : super(group, daemon)
    constructor(group: ThreadGroup, name: String) : super(group, name)
    constructor(group: ThreadGroup, name: String, daemon: Boolean) : super(group, name, daemon)

    init {
        ringBuffer.addGatingSequences(poller.sequence)
    }

    final override fun run() {
        logger.debug("Starting $name")
        try {
            startExecuting()
            while (!shouldExit()) {
                loop()
            }
            queueFinalized.set(true)
            workQueue()
            stopExecuting()
        } finally {
            logger.debug("Stopping $name")
        }
    }

    protected fun loop() {

    }

    protected fun signal() {

    }

    override fun <T> submit(callable: GameCallable<T>): CompletableFuture<T> {
        val fut = CompletableFuture<T>()
        if (currentThread() == this) {
            work(callable, fut)
        } else {
            ringBuffer.publishEvent(
                { event, _, f, entry, call -> event.set(entry, call, f) },
                fut,
                StackTraceSnapshot.new(),
                callable
            )
            signal()
        }
        return fut
    }

    fun exit(): CompletableFuture<Unit>? {
        exit.set(true)
        signal()
        return null
    }

    final override fun cleanup0(): CompletableFuture<Unit>? = exit()

    protected open fun shouldExit(): Boolean {
        return exit.get()
    }

    private fun <T> work(call: GameCallable<T>, future: CompletableFuture<T>) {
        try {
            future.complete(call.call())
        } catch (ex: Throwable) {
            val ex2 = buildStackTrace()
            ex2.initCause(ex)
            logger.error("Failed to execute task $call", ex2)
            future.completeExceptionally(ex)
        }
    }

    fun buildStackTrace(): GameException {
        val ex = GameException("Exception in ExecutorThread")
        ex.stackTrace = emptyArray()
        val c = cause
        if (c != null) {
            val t = c.buildCause()
            ex.addSuppressed(t)
        }
        return ex
    }

    protected fun workQueue() {
        poller.poll { e, sequence, endOfBatch ->
            if (Debug.calculateThreadStacks) cause = e.entry

            if (Debug.calculateThreadStacks) cause = null
            true
        }
    }

    protected open fun startExecuting() {}
    protected open fun workExecution() {}
    protected open fun stopExecuting() {}

    private class QueueEntry<T>(
        var entry: StackTraceSnapshot? = null,
        var call: GameCallable<T>? = null,
        var future: CompletableFuture<T>? = null
    ) {
        @Suppress("UNCHECKED_CAST")
        fun <V> set(entry: StackTraceSnapshot?, call: GameCallable<V>?, future: CompletableFuture<V>?) {
            this.entry = entry
            this.call = call as GameCallable<T>?
            this.future = future as CompletableFuture<T>?
        }

        fun clear() {
            entry = null
            call = null
            future = null
        }
    }
}

