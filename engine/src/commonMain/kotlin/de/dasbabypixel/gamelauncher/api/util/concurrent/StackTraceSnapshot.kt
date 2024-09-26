package de.dasbabypixel.gamelauncher.api.util.concurrent

import de.dasbabypixel.gamelauncher.api.util.Debug
import de.dasbabypixel.gamelauncher.api.util.GameException

class StackTraceSnapshot(
    val stacktrace: Collection<StackTraceElement>?, val cause: StackTraceSnapshot?, val thread: Thread
) {
    companion object {
        fun new(): StackTraceSnapshot {
            val thread = currentThread()
            val cause = cause(thread)
            if (Debug.calculateThreadStacks) {
                val stack = thread.stackTrace.drop(2)
                return StackTraceSnapshot(stack, cause, thread)
            }
            return StackTraceSnapshot(null, cause, thread)
        }

        private fun cause(thread: Thread): StackTraceSnapshot? {
            if (thread is CauseContainer) {
                return thread.cause
            }
            return null
        }
    }

    fun buildCause(): Throwable {
        val c = GameException("Thread ${thread.name}")
        if (stacktrace != null) c.stackTrace = stacktrace.toTypedArray()
        else c.stackTrace = emptyArray()
        if (cause != null) c.initCause(cause.buildCause())
        return c
    }

    override fun toString(): String {
        return "StackTraceSnapshot(stacktrace=$stacktrace, cause=$cause, thread=$thread)"
    }

    interface CauseContainer {
        val cause: StackTraceSnapshot?
    }
}