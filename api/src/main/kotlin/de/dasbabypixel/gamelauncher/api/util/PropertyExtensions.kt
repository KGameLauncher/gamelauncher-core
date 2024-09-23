package de.dasbabypixel.gamelauncher.api.util

fun Boolean.Companion.getBoolean(name: String): Boolean {
    return java.lang.Boolean.getBoolean(name)
}