package de.dasbabypixel.gamelauncher.api.util.concurrent

import de.dasbabypixel.gamelauncher.api.util.extension.invoke
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class FrameSync {
    private val frameSync: StateSynchronizer
    private val signalSync: StateSynchronizer

    // -1 = unlimited, 0 = vsync, -2=on-demand
    private var framerate = 0
    private val hasNewFramerate = AtomicBoolean(true)
    private val newFramerate = AtomicInteger(15)
    private val incrementFrames = AtomicInteger(0)
    private val frameTracker = FrameTracker()

    constructor(
        frameSync: StateSynchronizer = StateSynchronizer(), signalSync: StateSynchronizer = StateSynchronizer()
    ) {
        this.frameSync = frameSync
        this.signalSync = signalSync
    }

    fun syncStart() {
        if (hasNewFramerate.compareAndSet(true, false)) {
            setFramerate(newFramerate.get())
        }
        frameTracker.syncStart()
    }

    fun syncEnd() {
        frameTracker.syncEnd()
        frameSync.next()
    }

    fun waitForNextFrame() {
        frameSync.awaitNext()
    }

    fun waitForFrame(frame: Long) {
        frameSync.await(frame)
    }

    fun startNextFrame(frames: Int = 1): Long {
        val next = frameSync.nextState(frames)
        if (frames > 1) incrementFrames.addAndGet(frames)
        signalSync.next()
        return next
    }

    fun updateFramerate(framerate: Int) {
        this.newFramerate(framerate)
        this.hasNewFramerate(true)
    }

    private fun setFramerate(framerate: Int) {
        frameTracker.frameTimeNanos = when (framerate) {
            0 -> 0L
            -1 -> 0L
            -2 -> -1L
            else -> 1_000_000_000L / framerate
        }
    }

    private inner class FrameTracker {
        private var firstStart = true
        var lastFrameStartNanos: Long = 0L
        var lastFrameEndNanos: Long = 0L
        private var stateBegin: Long = 0L
        private var slept = false
        var frameTimeNanos = 0L

        fun syncStart() {
            if (slept) {
                slept = false
                return // If we had to sleep, then use a calculated lastFrameStartNanos instead
            }
            stateBegin = signalSync.nextState(0)
            lastFrameStartNanos = System.nanoTime()
        }

        fun syncEnd() {
            lastFrameEndNanos = System.nanoTime()
            if (firstStart) {
                firstStart = false
                return
            }
            if (incrementFrames.get() > 0) {
                incrementFrames.decrementAndGet()
                return // We don't sleep here
            }

            if (frameTimeNanos == -1L) {
                signalSync.await(stateBegin + 1)
                return
            }

            if (lastFrameStartNanos > System.nanoTime()) {
                lastFrameStartNanos = lastFrameEndNanos
            }
            val expectedFrameEndTime = lastFrameStartNanos + frameTimeNanos
            val sleepTime = expectedFrameEndTime - lastFrameEndNanos
            if (sleepTime > 1000) { // Only if we want to park at least 1 microsecond. Smaller doesn't make any sense
                signalSync.awaitNext(nanos = sleepTime)
                slept = true
//                println(
//                    "Slept ${TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - lastFrameStartNanos)}ms, should be ${
//                        TimeUnit.NANOSECONDS.toMillis(
//                            sleepTime
//                        )
//                    }ms"
//                )
                lastFrameStartNanos += frameTimeNanos
            }
        }
    }
}
