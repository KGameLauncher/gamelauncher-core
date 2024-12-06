import de.dasbabypixel.gamelauncher.gradle.JvmArgumentGenerator
import de.dasbabypixel.gamelauncher.gradle.lwjglDefaultDevArgs
import de.dasbabypixel.gamelauncher.gradle.lwjglDefaultDevInitSystemProperties
import de.dasbabypixel.gamelauncher.gradle.lwjglMain
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

plugins {
    id("gamelauncher-lwjgl")
    `maven-publish`
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.assignment)
    alias(libs.plugins.graal.native)
    alias(libs.plugins.android.library)
}

android {
    namespace = "de.dasbabypixel.gamelauncher.android"
    setCompileSdkVersion(34)
}

kotlin {
    jvmToolchain {
        this.languageVersion = JavaLanguageVersion.of(23)
        this.vendor = JvmVendorSpec.ADOPTIUM
    }
//    jvm("lwjgl") {
//    }
    jvm("lwjgl")
    androidTarget("android") {
    }
    @OptIn(ExperimentalKotlinGradlePluginApi::class) compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
    sourceSets {
        commonMain {
            dependencies {
                api(libs.bundles.logging)
                api(libs.disruptor)
                api(libs.joml)
            }
        }
        commonTest {
            resources.srcDir(file("src/lwjglTest/generatedres"))
            dependencies {
                api(kotlin("test"))
            }
        }
        val commonImplMain = sourceSets.register("commonImplMain") {
            dependsOn(commonMain.get())
        }
        val commonImplTest = sourceSets.register("commonImplTest") {
            dependsOn(commonTest.get())
        }
        named("lwjglMain") {
            dependsOn(commonImplMain.get())
            dependencies {
                api("org.lwjgl:lwjgl:${lwjgl.version}")
                api("org.lwjgl:lwjgl-glfw:${lwjgl.version}")
                api("org.lwjgl:lwjgl-opengl:${lwjgl.version}")
                api("org.lwjgl:lwjgl-opengles:${lwjgl.version}")
                api("org.lwjgl:lwjgl-stb:${lwjgl.version}")

                runtimeOnly("org.lwjgl:lwjgl:${lwjgl.version}:${lwjgl.natives}") {
                    this.artifact {
                        this.classifier = lwjgl.natives
                    }
                }
                runtimeOnly("org.lwjgl:lwjgl-glfw:${lwjgl.version}:${lwjgl.natives}") {
                    this.artifact {
                        this.classifier = lwjgl.natives
                    }
                }
                runtimeOnly("org.lwjgl:lwjgl-opengl:${lwjgl.version}:${lwjgl.natives}") {
                    this.artifact {
                        this.classifier = lwjgl.natives
                    }
                }
                runtimeOnly("org.lwjgl:lwjgl-opengles:${lwjgl.version}:${lwjgl.natives}") {
                    this.artifact {
                        this.classifier = lwjgl.natives
                    }
                }
                runtimeOnly("org.lwjgl:lwjgl-stb:${lwjgl.version}:${lwjgl.natives}") {
                    this.artifact {
                        this.classifier = lwjgl.natives
                    }
                }
//    "graalImplementation"("org.graalvm.sdk:graal-sdk:24.1.0")
//    "graalCompileOnly"(sourceSets.main.map { it.output })

                api(libs.bundles.logging.runtime)
                api(libs.bundles.jline)
            }
        }
        named("lwjglTest") {
            dependsOn(commonImplTest.get())
            dependencies {
                api(libs.junit.jupiter.api)
                api(libs.junit.jupiter.engine)
            }
        }
//        named("androidMain") {
//            dependsOn(commonImplMain.get())
//        }
    }
}

tasks {
    val genDevInitSysProps = register<JvmArgumentGenerator>("genDevInitSysProps") {
        arguments.addAll(lwjglDefaultDevInitSystemProperties.map { it.key + "=" + it.value })
        outputFile.set(file("src/lwjglTest/generatedres/gamelauncher.sysprops"))
    }
    named("lwjglTestProcessResources") {
        dependsOn(genDevInitSysProps)
    }
    afterEvaluate {
        // Double afterEvaluate to ensure we overwrite everything in multiplatform.
        // One afterEvaluate was not enough
        afterEvaluate {
            named<JavaExec>("lwjglRun") {
                outputs.upToDateWhen { false }
                val lwjglTarget = kotlin.targets.getByName<KotlinJvmTarget>("lwjgl")
                val mainCompilation = lwjglTarget.compilations["main"]
                classpath(mainCompilation.output.allOutputs)
                classpath(mainCompilation.runtimeDependencyFiles)
                workingDir(rootProject.mkdir("run"))
                mainClass = lwjglMain
                jvmArgs(lwjglDefaultDevArgs)
                jvmArgs(lwjglDefaultDevInitSystemProperties.map { "-D" + it.key + "=" + it.value })
                jvmArgs("-Dgamelauncher.skipsysprops=true")
                jvmArgs(
                    "-Dstdout.encoding=${System.out.charset().name()}",
                    "-Dstderr.encoding=${System.err.charset().name()}"
                )
                standardInput = System.`in`
                standardOutput = System.out
                errorOutput = System.err
            }
        }
    }
}
