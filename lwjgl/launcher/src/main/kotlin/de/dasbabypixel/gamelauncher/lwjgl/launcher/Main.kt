package de.dasbabypixel.gamelauncher.lwjgl.launcher

import sun.misc.Signal
import java.lang.management.ManagementFactory
import java.lang.management.RuntimeMXBean
import java.nio.file.Path
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.concurrent.thread

fun main() {
    val runtime = ManagementFactory.getRuntimeMXBean()
    val currentJar = Path.of(Main::class.java.protectionDomain.codeSource.location.toURI()).toAbsolutePath().toString()

    val arguments = ArrayList<String>()
    arguments.add(resolveJavaExecutable())
    arguments.add("--enable-native-access=ALL-UNNAMED")
    arguments.add("--add-opens=java.base/jdk.internal.io=ALL-UNNAMED")

    arguments.add("-cp")
    arguments.add(buildClassPath(runtime, currentJar))
    arguments.add("de.dasbabypixel.gamelauncher.lwjgl.MainKt")

    Signal.handle(Signal("INT")) {
    }

    thread {
        val process = ProcessBuilder(*arguments.toTypedArray()).inheritIO().start()

        Runtime.getRuntime().addShutdownHook(Thread {
            val processHandle = process.toHandle()
            if (processHandle.isAlive && processHandle.destroy()) {
                try {
                    processHandle.onExit().get(5, TimeUnit.SECONDS)
                } catch (_: ExecutionException) {
                    processHandle.destroyForcibly()
                } catch (_: TimeoutException) {
                    processHandle.destroyForcibly()
                } catch (_: InterruptedException) {
                    Thread.currentThread().interrupt()
                }
            }
        })

        process.waitFor()
    }
}

private fun buildClassPath(runtime: RuntimeMXBean, currentJar: String): String {
    return runtime.classPath
}

private fun resolveJavaExecutable(): String {
    // java.home points to the root java installation directory
    return Path.of(System.getProperty("java.home"), "bin", "java").toAbsolutePath().toString()
}

class Main