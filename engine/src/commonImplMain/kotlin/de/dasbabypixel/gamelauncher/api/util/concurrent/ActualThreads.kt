package de.dasbabypixel.gamelauncher.api.util.concurrent

import de.dasbabypixel.gamelauncher.common.util.concurrent.CommonAbstractThread
import de.dasbabypixel.gamelauncher.common.util.concurrent.CommonThreadGroup
import de.dasbabypixel.gamelauncher.common.util.concurrent.CommonThreadMethods

actual typealias AbstractThread = CommonAbstractThread
actual typealias ThreadMethods = CommonThreadMethods

actual sealed interface ThreadGroup {
    actual val name: String
    actual val parent: ThreadGroup?

    actual companion object {
        actual fun create(name: String): ThreadGroup {
            return CommonThreadGroup(name)
        }

        actual fun create(name: String, parent: ThreadGroup): ThreadGroup {
            return CommonThreadGroup(name, parent)
        }
    }
}

internal interface CThreadGroup : ThreadGroup
