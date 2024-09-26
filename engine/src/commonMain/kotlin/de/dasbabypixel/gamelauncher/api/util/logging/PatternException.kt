package de.dasbabypixel.gamelauncher.api.util.logging

class PatternException(text: String) : RuntimeException(text) {
    constructor(e: PatternException, suffix: String) : this(e.message + suffix) {
        stackTrace = e.stackTrace
    }
}
