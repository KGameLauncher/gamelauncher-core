package de.dasbabypixel.gamelauncher.lwjgl.window

import de.dasbabypixel.gamelauncher.api.util.logging.getLogger
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWMonitorCallback
import java.util.concurrent.CopyOnWriteArrayList

object GLFWMonitors {
    private val logger = getLogger<GLFWMonitors>("LWJGL")
    private val callback = object : GLFWMonitorCallback() {
        override fun invoke(monitor: Long, event: Int) {
            when (event) {
                GLFW_CONNECTED -> connectMonitor(monitor)
                GLFW_DISCONNECTED -> disconnectMonitor(monitor)
                else -> logger.error("Unknown event: $event")
            }
        }
    }
    private val monitors = CopyOnWriteArrayList<GLFWMonitor>()

    init {
        GLFWThread.ensureOnThread()
        glfwSetMonitorCallback(callback)
        val monitors = glfwGetMonitors() ?: throw IllegalStateException("Unable to fetch monitors")
        while (monitors.hasRemaining()) {
            connectMonitor(monitors.get())
        }
    }

    private fun connectMonitor(monitorId: Long) {
        GLFWThread.ensureOnThread()
        val x = intArrayOf(0)
        val y = intArrayOf(0)
        glfwGetMonitorPos(monitorId, x, y)
        val name = glfwGetMonitorName(monitorId) ?: throw IllegalStateException("Monitor without name")
        val sx = floatArrayOf(0F)
        val sy = floatArrayOf(0F)
        glfwGetMonitorContentScale(monitorId, sx, sy)
        val vidMode = glfwGetVideoMode(monitorId) ?: throw IllegalStateException("Monitor without VideoMode")
        val monitor = GLFWMonitor(
            name,
            x[0],
            y[0],
            vidMode.width(),
            vidMode.height(),
            sx[0],
            sy[0],
            monitorId,
            VideoMode(vidMode.width(), vidMode.height(), vidMode.refreshRate())
        )
        monitors.add(monitor)
        logger.info("New monitor connected: {}", monitor)
    }

    private fun disconnectMonitor(monitor: Long) {
        GLFWThread.ensureOnThread()
        if(monitors.removeIf {
            it.glfwId == monitor
        }) {
            logger.info("Monitor disconnected: {}", monitor)
        }
    }
}

data class GLFWMonitor(
    val name: String,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
    val scaleX: Float,
    val scaleY: Float,
    val glfwId: Long,
    val videoMode: VideoMode
)

data class VideoMode(
    val width: Int, val height: Int, val refreshRate: Int
)