package de.dasbabypixel.gamelauncher.lwjgl.window

interface WindowRenderImplementation {
    fun enable(window: GLFWWindow)
    fun disable(window: GLFWWindow)
}

class DoubleBufferedAsyncRenderer : WindowRenderImplementation {
    override fun enable(window: GLFWWindow) {
        TODO("Not yet implemented")
    }

    override fun disable(window: GLFWWindow) {
        TODO("Not yet implemented")
    }
}