package de.dasbabypixel.gamelauncher.api.util.concurrent

import de.dasbabypixel.gamelauncher.common.util.concurrent.CommonThreadHelper

actual fun currentThread(): Thread {
    return CommonThreadHelper.currentThread()
}