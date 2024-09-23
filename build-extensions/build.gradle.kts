plugins {
    `kotlin-dsl`
    id("java-gradle-plugin")
}

repositories {
    gradlePluginPortal()
}

gradlePlugin {
    plugins {
        register("gamelauncherParent") {
            id = "gamelauncher-parent"
            implementationClass = "de.dasbabypixel.gamelauncher.gradle.GameLauncherParent"
        }
        register("gamelauncherLWJGL") {
            id = "gamelauncher-lwjgl"
            implementationClass = "de.dasbabypixel.gamelauncher.gradle.GameLauncherLWJGL"
        }
    }
}