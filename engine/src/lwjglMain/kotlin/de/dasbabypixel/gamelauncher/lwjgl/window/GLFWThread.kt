package de.dasbabypixel.gamelauncher.lwjgl.window

import de.dasbabypixel.gamelauncher.api.util.concurrent.AbstractExecutorThread
import de.dasbabypixel.gamelauncher.api.util.concurrent.ThreadGroup
import de.dasbabypixel.gamelauncher.api.util.logging.getLogger
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.system.APIUtil

val glThreadGroup = ThreadGroup.create("gl")

object GLFWThread : AbstractExecutorThread(glThreadGroup, "GLFW-Thread") {
    private val logger = getLogger<GLFWThread>("LWJGL")

    @Volatile
    private var initialized = false

    @OptIn(ExperimentalStdlibApi::class)
    private val errorCallback = object : GLFWErrorCallback() {
        private val ERROR_CODES = APIUtil.apiClassTokens(
            { _, value -> value in 0x10001..0x1ffff }, null, org.lwjgl.glfw.GLFW::class.java
        )

        override fun invoke(errorCode: Int, descriptionId: Long) {
            val description = getDescription(descriptionId)
            val error = ERROR_CODES[errorCode]!!
            logger.error("GLFW Error: {}({}) - {}", error, errorCode.toHexString(), description, Exception())
        }
    }

    override fun startExecuting() {
        logger.debug("Initializing GLFW")
        glfwSetErrorCallback(errorCallback)
        if (!glfwInit()) {
            throw IllegalStateException("Failed to initialize GLFW")
        }
        initialized = true
    }

    override fun workExecution() {
        glfwPollEvents()
    }

    override fun waitForSignal() {
        if (hasWorkBool.compareAndSet(true, false)) {
            lock.lock()
            try {

            } finally {
                lock.unlock()
            }
            return
        }
        logger.info("Wait")
        glfwWaitEvents()
    }

    override fun signal() {
        if (initialized) {
            glfwPostEmptyEvent()
        } else {
            lock.lock()
            try {
                if (initialized) {
                    glfwPostEmptyEvent()
                    return
                }
                hasWorkBool.set(true)
                hasWork.signal()
            } finally {
                lock.unlock()
            }
        }
    }

    override fun stopExecuting() {
        logger.debug("Terminating GLFW")
        errorCallback.free()
        initialized = false
        glfwTerminate()
    }
}
