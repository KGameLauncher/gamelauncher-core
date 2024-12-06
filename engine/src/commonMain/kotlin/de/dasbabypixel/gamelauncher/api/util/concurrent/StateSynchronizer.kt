package de.dasbabypixel.gamelauncher.api.util.concurrent

import de.dasbabypixel.gamelauncher.api.util.function.GameBiConsumer
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

class StateSynchronizer {
    private val lock: Lock
    private val condition: Condition
    private var state = 0L
    var sleep: GameBiConsumer<Condition, Long> = GameBiConsumer { condition, nanos ->
        if (nanos < 0) {
            condition.awaitUninterruptibly()
            return@GameBiConsumer
        } else if (nanos == 0L) return@GameBiConsumer
        val end = System.nanoTime() + nanos
        while (true) {
            val nanoWait = end - System.nanoTime()
            if (nanoWait <= 1000) break // +-1000ns is acceptable to improve performance
            try {
                condition.awaitNanos(nanoWait)
                return@GameBiConsumer
            } catch (_: InterruptedException) {
            }
        }
    }

    constructor(
        lock: Lock = ReentrantLock(), condition: Condition = lock.newCondition()
    ) {
        this.lock = lock
        this.condition = condition
    }

    fun await(state: Long) {
        val lock = this.lock
        val condition = this.condition
        val sleep = this.sleep
        lock.lock()
        try {
            while (true) {
                if (this.state - state >= 0) return
                sleep.accept(condition, -1)
            }
        } finally {
            lock.unlock()
        }
    }

    fun awaitNext(steps: Int = 1, nanos: Long = -1L) {
        val lock = this.lock
        val condition = this.condition
        val sleep = this.sleep
        val end = System.nanoTime() + nanos
        lock.lock()
        val state = this.state + steps
        try {
            while (true) {
                val nanoWait = if (nanos < 0) -1L else end - System.nanoTime()
                if (nanoWait <= 1000) return // -+1000ns is acceptable for performance
                if (this.state - state >= 0) return
                sleep.accept(condition, nanoWait)
            }
        } finally {
            lock.unlock()
        }
    }

    fun nextState(steps: Int = 1): Long {
        val lock = this.lock
        lock.lock()
        try {
            return state + steps
        } finally {
            lock.unlock()
        }
    }

    fun next(): Long {
        val lock = this.lock
        val s: Long
        lock.lock()
        try {
            s = ++state
            condition.signalAll()
        } finally {
            lock.unlock()
        }
        return s
    }
}