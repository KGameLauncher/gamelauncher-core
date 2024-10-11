package de.dasbabypixel.gamelauncher.api.util.concurrent

import com.lmax.disruptor.RingBuffer
import com.lmax.disruptor.SleepingWaitStrategy
import de.dasbabypixel.gamelauncher.api.util.Debug
import de.dasbabypixel.gamelauncher.api.util.GameException
import de.dasbabypixel.gamelauncher.api.util.function.GameCallable
import de.dasbabypixel.gamelauncher.api.util.logging.getLogger
import de.dasbabypixel.gamelauncher.api.util.resource.AbstractGameResource
import java.util.concurrent.CompletableFuture
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock

expect abstract class AbstractThread : AbstractGameResource, Thread {
    constructor(group: ThreadGroup)
    constructor(group: ThreadGroup, daemon: Boolean)
    constructor(group: ThreadGroup, name: String)
    constructor(group: ThreadGroup, name: String, daemon: Boolean)

    final override val stackTrace: Array<StackTraceElement>
    final override val name: String
    final override val group: ThreadGroup
    override val autoTrack: Boolean

    final override fun start()
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

    @Volatile
    private var exit = false
    protected val lock = ReentrantLock()
    protected val count = AtomicInteger(0)
    protected val hasWork = lock.newCondition()
    protected val hasWorkBool = AtomicBoolean(false)

    constructor(group: ThreadGroup) : super(group)
    constructor(group: ThreadGroup, daemon: Boolean) : super(group, daemon)
    constructor(group: ThreadGroup, name: String) : super(group, name)
    constructor(group: ThreadGroup, name: String, daemon: Boolean) : super(group, name, daemon)

    init {
        ringBuffer.addGatingSequences(poller.sequence)
    }

    /**
     * Function to start thread via `Thread#start`.
     * IntelliJ doesn't seem to work well with multiplatform and inheritance, so this is a workaround
     */
    fun startThread() = start()

    final override fun run() {
        logger.debug("Starting $name")
        try {
            startExecuting()
            while (!shouldExit()) {
                loop()
            }
            workQueue()
            workExecution()
            stopExecuting()
        } catch (t: Throwable) {
            logger.error("Exception in thread {}", thread.name, t)
        } finally {
            logger.debug("Stopping $name")
        }
    }

    private fun loop() {
        if (shouldWaitForSignal()) {
            waitForSignal()
        }
        workQueue()
        workExecution()
    }

    protected open fun signal() {
        try {
            lock.lock()
            hasWorkBool.set(true)
            hasWork.signal()
        } finally {
            lock.unlock()
        }
    }

    protected open fun waitForSignal() {
        try {
            lock.lock()
            while (!hasWorkBool.compareAndSet(true, false)) {
                hasWork.awaitUninterruptibly()
            }
        } finally {
            lock.unlock()
        }
    }

    protected open fun shouldWaitForSignal(): Boolean {
        return true
    }

    override fun <T> submit(callable: GameCallable<T>): CompletableFuture<T> {
        if (exit) throw RejectedExecutionException("No new tasks can be submitted. The executor has been shut down")
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
            if (exit) throw RejectedExecutionException("No new tasks can be submitted. The executor has been shut down")
            signal()
        }
        return fut
    }

    fun exit(): CompletableFuture<Unit>? {
        exit = true
        signal()
        return null
    }

    final override fun cleanup0(): CompletableFuture<Unit>? = exit()

    protected open fun shouldExit(): Boolean {
        return exit
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
            try {
                e.execute()
            } catch (t: Throwable) {
                val ex = buildStackTrace()
                ex.stackTrace = t.stackTrace
                ex.initCause(t)
                logger.error("Failed to execute task {}", e.call, ex)
            }
            e.clear()
            if (Debug.calculateThreadStacks) cause = null
            true
        }
    }

    protected open fun startExecuting() {}
    protected open fun workExecution() {}
    protected open fun stopExecuting() {}

    private class QueueEntry<T : Any>(
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

        fun execute() {
            future!!.complete(call!!.call())
        }

        fun clear() {
            entry = null
            call = null
            future = null
        }
    }
}

