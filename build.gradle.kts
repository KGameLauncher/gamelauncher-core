import org.jetbrains.gradle.ext.Application
import org.jetbrains.gradle.ext.runConfigurations
import org.jetbrains.gradle.ext.settings

plugins {
    id("gamelauncher-parent")
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.launch4j) apply false
    alias(libs.plugins.graal.native) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.shadow) apply false
    alias(libs.plugins.idea.ext)
}

println("ää")
System.getProperties().forEach { t, u -> println("$t: $u") }
println(System.out.charset())

idea.project.settings {
    runConfigurations {
        register<Application>("Main") {
            mainClass = "de.dasbabypixel.gamelauncher.lwjgl.MainKt"
            moduleName = "gamelauncher.engine.lwjglMain"
            workingDirectory = "run"
        }
    }
}