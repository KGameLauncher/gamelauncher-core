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

    val arguments = ArrayList<String>()
    arguments.add(resolveJavaExecutable())
    arguments.addAll(readJvmArgsFile())

    arguments.add("-cp")
    arguments.add(buildClassPath(runtime))
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

private fun readJvmArgsFile(): List<String> {
    return Main::class.java.classLoader.getResourceAsStream("gamelauncher.jvmargs").use { `in` ->
        `in`?.bufferedReader().use { reader ->
            reader?.readLines() ?: emptyList()
        }
    }
}

private fun buildClassPath(runtime: RuntimeMXBean): String {
    return runtime.classPath
}

private fun resolveJavaExecutable(): String {
    // java.home points to the root java installation directory
    return Path.of(System.getProperty("java.home"), "bin", "java").toAbsolutePath().toString()
}

class Main