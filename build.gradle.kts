import org.jetbrains.gradle.ext.Application
import org.jetbrains.gradle.ext.runConfigurations
import org.jetbrains.gradle.ext.settings

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.launch4j) apply false
    alias(libs.plugins.graal.native) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.idea.ext)
}

idea.project.settings {
    runConfigurations {
        register<Application>("Main") {
            mainClass = "de.dasbabypixel.gamelauncher.lwjgl.MainKt"
            moduleName = "gamelauncher.engine.lwjglMain"
            workingDirectory = "run"
        }
    }
}