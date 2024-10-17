package de.dasbabypixel.gamelauncher.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.listProperty

abstract class JvmArgumentGenerator : DefaultTask() {
    @Input
    val arguments: ListProperty<String> = project.objects.listProperty<String>()

    @OutputFile
    val outputFile: RegularFileProperty = project.objects.fileProperty()

    init {
        outputFile.convention { temporaryDir.resolve("gamelauncher.jvmargs") }
    }

    @TaskAction
    fun execute() {
        val writer = outputFile.get().asFile.printWriter()
        for(argument in arguments.get()) {
            writer.println(argument)
        }
        writer.close()
    }
}