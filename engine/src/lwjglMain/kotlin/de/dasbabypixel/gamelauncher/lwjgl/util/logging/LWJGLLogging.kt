package de.dasbabypixel.gamelauncher.lwjgl.util.logging

import de.dasbabypixel.gamelauncher.api.GameLauncher
import de.dasbabypixel.gamelauncher.api.config.Config
import de.dasbabypixel.gamelauncher.api.util.Debug
import de.dasbabypixel.gamelauncher.api.util.extension.sleep
import de.dasbabypixel.gamelauncher.api.util.logging.Logging
import de.dasbabypixel.gamelauncher.api.util.logging.getLogger
import org.jline.reader.EndOfFileException
import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import org.jline.reader.UserInterruptException
import org.jline.terminal.TerminalBuilder
import java.lang.management.ManagementFactory
import java.nio.charset.Charset
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.concurrent.thread

class LWJGLLogging {
    companion object {
        fun init() {
            val useAnsi = Config.USE_ANSI.value
            Logger.getLogger("org.jline").level = Level.ALL

            val runtime = ManagementFactory.getRuntimeMXBean()
            runtime.inputArguments.forEach {
                println(it)
            }

            var requestExit = false
            val terminal = TerminalBuilder.builder().system(true).signalHandler {
                Logging.out.println("Receive signal ${it.name} - ${it.ordinal}")
                requestExit = true
            }.ffm(true).apply { if (Debug.inIde) dumb(true) }
                .encoding(System.console()?.charset() ?: Charset.defaultCharset()).build()
            val reader = LineReaderBuilder.builder().appName("GameLauncher").terminal(terminal).build()
            Log4jConfiguration.setup(useAnsi, reader)
            reader.option(LineReader.Option.AUTO_GROUP, false)
            reader.option(LineReader.Option.AUTO_MENU_LIST, true)
            reader.option(LineReader.Option.AUTO_FRESH_LINE, true)
            reader.option(LineReader.Option.EMPTY_WORD_OPTIONS, false)
            reader.option(LineReader.Option.HISTORY_TIMESTAMPED, false)
            reader.option(LineReader.Option.DISABLE_EVENT_EXPANSION, true)

            reader.variable(LineReader.BELL_STYLE, "none")
            reader.variable(LineReader.HISTORY_SIZE, 500)
            reader.variable(LineReader.HISTORY_FILE_SIZE, 2500)
            reader.variable(LineReader.COMPLETION_STYLE_LIST_BACKGROUND, "inverse")

            val logger = getLogger<LWJGLLogging>()
            thread(name = "Console Thread") {
                while (true) {
                    try {
                        if (requestExit) throw UserInterruptException("")
                        val prompt = if (Debug.inIde) null else "Prompt: "
                        val line = reader.readLine(prompt) ?: break
                        if (line == "exit") GameLauncher.stop()
                        logger.info("Read $line")
                    } catch (_: EndOfFileException) {
                    } catch (e: UserInterruptException) {
                        Logging.out.println("Interrupted")
                        sleep(1000)
                        GameLauncher.stop()
                    } catch (t: Throwable) {
                        logger.error("Failed to read line, exiting", t)
                        GameLauncher.handleException(t)
                    }
                }
            }
        }
    }
}
