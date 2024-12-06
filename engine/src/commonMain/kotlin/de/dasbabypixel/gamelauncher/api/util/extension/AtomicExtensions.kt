package de.dasbabypixel.gamelauncher.api.util.extension

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

operator fun AtomicInteger.invoke(num: Int) = set(num)
operator fun AtomicLong.invoke(num: Long) = set(num)
operator fun AtomicBoolean.invoke(s: Boolean) = set(s)
