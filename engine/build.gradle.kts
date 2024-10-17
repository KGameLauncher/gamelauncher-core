import de.dasbabypixel.gamelauncher.gradle.lwjglDefaultDevArgs
import de.dasbabypixel.gamelauncher.gradle.lwjglMain
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

plugins {
    id("gamelauncher-lwjgl")
    `maven-publish`
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.graal.native)
}

kotlin {
    jvmToolchain(23)
    jvm("lwjgl")
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
                api(project.dependencies.platform("org.lwjgl:lwjgl-bom:${lwjgl.version}"))
                api("org.lwjgl:lwjgl")
                api("org.lwjgl:lwjgl-glfw")
                api("org.lwjgl:lwjgl-opengl")
                api("org.lwjgl:lwjgl-opengles")
                api("org.lwjgl:lwjgl-stb")

                runtimeOnly("org.lwjgl:lwjgl:${lwjgl.version}:${lwjgl.natives}")
                runtimeOnly("org.lwjgl:lwjgl-glfw:${lwjgl.version}:${lwjgl.natives}")
                runtimeOnly("org.lwjgl:lwjgl-opengl:${lwjgl.version}:${lwjgl.natives}")
                runtimeOnly("org.lwjgl:lwjgl-opengles:${lwjgl.version}:${lwjgl.natives}")
                runtimeOnly("org.lwjgl:lwjgl-stb:${lwjgl.version}:${lwjgl.natives}")

//    "graalImplementation"("org.graalvm.sdk:graal-sdk:24.1.0")
//    "graalCompileOnly"(sourceSets.main.map { it.output })

                implementation(libs.bundles.logging.runtime)
                implementation(libs.bundles.jline)
            }
        }
        named("lwjglTest") {
            dependsOn(commonImplTest.get())
            dependencies {
                implementation(libs.junit.jupiter.api)
                runtimeOnly(libs.junit.jupiter.engine)
            }
        }
    }
}

tasks {
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
                standardInput = System.`in`
                standardOutput = System.out
                errorOutput = System.err
            }
        }
    }
}
