package de.dasbabypixel.gamelauncher.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

class GameLauncherLWJGL : Plugin<Project> {
    override fun apply(project: Project) {
        val osName = System.getProperty("os.name")!!
        val osArch = System.getProperty("os.arch")!!

        val natives = when {
            "FreeBSD" == osName -> "natives-freebsd"
            arrayOf("Linux", "SunOS", "Unit").any { osName.startsWith(it) } -> {
                if (arrayOf(
                        "arm", "aarch64"
                    ).any { osArch.startsWith(it) }
                ) "natives-linux${if (osArch.contains("64") || osArch.startsWith("armv8")) "-arm64" else "-arm32"}"
                else if (osArch.startsWith("ppc")) "natives-linux-ppc64le"
                else if (osArch.startsWith("riscv")) "natives-linux-riscv64"
                else "natives-linux"
            }

            arrayOf("Mac OS X", "Darwin").any { osName.startsWith(it) } -> {
                "natives-macos${if (osArch.startsWith("aarch64")) "-arm64" else ""}"
            }

            arrayOf("Windows").any { osName.startsWith(it) } -> {
                if (osArch.contains("64")) "natives-windows${if (osArch.startsWith("aarch64")) "-arm64" else ""}"
                else "natives-windows-x86"
            }

            else -> throw Error("Unrecognized or unsupported platform.")
        }

        val versionCatalogsExtension: VersionCatalogsExtension = project.rootProject.extensions.getByType()
        val version = versionCatalogsExtension.named("libs").findVersion("lwjgl").orElseThrow { Error("Please specify lwjgl version in gradle/libs.versions.toml") }.displayName

        val extension = LWJGLExtension(natives, version)
        project.extensions.add("lwjgl", extension)


    }
}

data class LWJGLExtension(val natives: String, val version: String)