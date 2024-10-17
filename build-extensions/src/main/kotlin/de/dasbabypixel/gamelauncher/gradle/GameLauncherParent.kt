package de.dasbabypixel.gamelauncher.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class GameLauncherParent : Plugin<Project> {
    override fun apply(target: Project) {

    }
}

val launcherGroup = "launcher"
val lwjglMain = "de.dasbabypixel.gamelauncher.lwjgl.MainKt"
val lwjglLauncherMain = "de.dasbabypixel.gamelauncher.lwjgl.launcher.MainKt"
val lwjglDefaultArgs = listOf(
    "--enable-preview", "--enable-native-access=ALL-UNNAMED", "--add-opens=java.base/jdk.internal.io=ALL-UNNAMED"
)
val lwjglDefaultDevArgs = lwjglDefaultArgs.plus(
    listOf(
        "-Dgamelauncher.in_ide=true"
    )
)
