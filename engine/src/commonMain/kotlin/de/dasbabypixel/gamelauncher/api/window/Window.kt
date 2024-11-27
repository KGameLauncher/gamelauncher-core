package de.dasbabypixel.gamelauncher.api.window

import java.util.concurrent.CompletableFuture

interface Window {
    fun show(): CompletableFuture<Unit>
    fun hide(): CompletableFuture<Unit>
}