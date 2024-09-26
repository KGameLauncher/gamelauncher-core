package de.dasbabypixel.gamelauncher.common.util.concurrent

import de.dasbabypixel.gamelauncher.api.util.concurrent.CThreadGroup
import de.dasbabypixel.gamelauncher.api.util.concurrent.Thread
import de.dasbabypixel.gamelauncher.api.util.concurrent.ThreadGroup
import de.dasbabypixel.gamelauncher.api.util.concurrent.ThreadHolder
import de.dasbabypixel.gamelauncher.api.util.logging.getLogger
import de.dasbabypixel.gamelauncher.api.util.resource.AbstractGameResource
import de.dasbabypixel.gamelauncher.api.util.resource.ResourceTracker.stopTracking
import java.util.*
import java.util.concurrent.locks.LockSupport
import java.lang.Thread as JThread
import java.lang.ThreadGroup as JThreadGroup

class CommonThreadGroup : CThreadGroup {
    override val name: String
    override val parent: ThreadGroup
    val group: JThreadGroup

    constructor(group: JThreadGroup) {
        this.name = group.name
        this.parent = ThreadGroupCache[group.parent]
        this.group = group
    }

    constructor(name: String) : this(name, ThreadGroupCache[JThread.currentThread().threadGroup])

    constructor(name: String, parent: ThreadGroup) {
        this.name = name
        this.parent = parent
        this.group = JThreadGroup(ThreadGroupCache[parent], name)
    }
}

object ThreadGroupCache {
    private val map = WeakHashMap<JThreadGroup, ThreadGroup>()
    operator fun get(group: JThreadGroup): ThreadGroup {
        synchronized(map) {
            return map.computeIfAbsent(group, ::CommonThreadGroup)
        }
    }

    operator fun get(group: ThreadGroup): JThreadGroup {
        return (group as CommonThreadGroup).group
    }
}

object CommonThreadMethods {
    fun park() {
        LockSupport.park()
    }

    fun park(nanos: Long) {
        LockSupport.parkNanos(nanos)
    }

    fun sleep(millis: Long) {
        JThread.sleep(millis)
    }
}

abstract class CommonAbstractThread : AbstractGameResource, Thread {
    companion object {
        val logger = getLogger<CommonAbstractThread>()
    }

    private var threadImpl: ThreadImpl
    override val group: ThreadGroup
        get() = actualGroup
    private val actualGroup: ThreadGroup
    final override val name: String
        get() = threadImpl.name
    final override val stackTrace: Array<StackTraceElement>
        get() = threadImpl.stackTrace
    override val autoTrack: Boolean
        get() = false

    constructor(group: ThreadGroup) {
        this.threadImpl = ThreadImpl(group, ::run0)
        this.actualGroup = ThreadGroupCache[threadImpl.threadGroup]
    }

    constructor(group: ThreadGroup, name: String) {
        this.threadImpl = ThreadImpl(group, ::run0, name)
        this.actualGroup = ThreadGroupCache[threadImpl.threadGroup]
    }

    constructor(group: ThreadGroup, daemon: Boolean) : this(group) {
        threadImpl.isDaemon = daemon
    }

    constructor(group: ThreadGroup, name: String, daemon: Boolean) : this(group, name) {
        threadImpl.isDaemon = daemon
    }

    fun start() {
        threadImpl.start()
        track()
    }

    override fun unpark() {
        LockSupport.unpark(threadImpl)
    }

    private fun run0() {
        try {

        } catch (e: Throwable) {
            logger.error("Uncaught exception in $name", e)
            stopTracking()
        }
    }

    protected abstract fun run()

    protected class ThreadImpl : JThread {
        constructor(group: ThreadGroup, runnable: Runnable) : super(
            ThreadGroupCache[group], runnable
        )

        constructor(group: ThreadGroup, runnable: Runnable, name: String) : super(
            ThreadGroupCache[group], runnable, name
        )
    }
}

object CommonThreadHelper {
    fun currentThread(): Thread {
        val thread = JThread.currentThread()
        if (thread !is ThreadHolder) throw IllegalStateException("Current thread is not a ThreadHolder")
        return thread.thread
    }
}
