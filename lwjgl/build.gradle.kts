plugins {
    id("gamelauncher-parent")
    id("gamelauncher-lwjgl")
//    application
    alias(libs.plugins.kotlin.multiplatform)
//    alias(libs.plugins.shadow)
//    alias(libs.plugins.graal.native)
}

val main = "de.dasbabypixel.gamelauncher.lwjgl.MainKt"
val launcherMain = "de.dasbabypixel.gamelauncher.lwjgl.launcher.MainKt"
val defaultArgs = listOf(
    "--enable-native-access=ALL-UNNAMED",
    "--add-opens=java.base/jdk.internal.io=ALL-UNNAMED",
    "-Dgamelauncher.in_ide=true"
)

//application {
//    mainClass = launcherMain
//    applicationDefaultJvmArgs += defaultArgs
//}
//
//sourceSets {
//    register("graal")
//}

//configurations.named(sourceSets["graal"].compileClasspathConfigurationName) {
//    extendsFrom(configurations[sourceSets["main"].compileClasspathConfigurationName])
//}

//graalvmNative {
//    toolchainDetection = true
//
//    binaries {
//        named("main") {
//            javaLauncher = javaToolchains.launcherFor {
//                languageVersion = JavaLanguageVersion.of(23)
//                vendor = JvmVendorSpec.GRAAL_VM
//            }
//            imageName = "GameLauncher"
//            mainClass = main
//            useFatJar = true
//            runtimeArgs(application.applicationDefaultJvmArgs)
//            buildArgs("-O0") // Disable optimizations for faster build
//        }
//        forEach {
//            println(it.name)
//        }
//    }
//}

kotlin {
    jvmToolchain(23)
    jvm()
    mingwX64("windows") {
        binaries.executable()
    }
    linuxX64 {
        binaries.executable()
    }
    macosX64 {
        binaries.executable()
    }

    sourceSets {
        commonMain {
            dependencies {
//                implementation(platform("org.lwjgl:lwjgl-bom:${lwjgl.version}"))
//                implementation("org.lwjgl:lwjgl")
//                implementation("org.lwjgl:lwjgl-glfw")
//                implementation("org.lwjgl:lwjgl-opengl")
//                implementation("org.lwjgl:lwjgl-opengles")
//                implementation("org.lwjgl:lwjgl-stb")
//
//                runtimeOnly("org.lwjgl", "lwjgl", classifier = lwjgl.natives)
//                runtimeOnly("org.lwjgl", "lwjgl-glfw", classifier = lwjgl.natives)
//                runtimeOnly("org.lwjgl", "lwjgl-opengl", classifier = lwjgl.natives)
//                runtimeOnly("org.lwjgl", "lwjgl-opengles", classifier = lwjgl.natives)
//                runtimeOnly("org.lwjgl", "lwjgl-stb", classifier = lwjgl.natives)
//                implementation(libs.joml)
//
////    "graalImplementation"("org.graalvm.sdk:graal-sdk:24.1.0")
////    "graalCompileOnly"(sourceSets.main.map { it.output })
//
//                implementation(projects.common)
//                implementation(libs.bundles.logging.runtime)
//                implementation(libs.disruptor)
//                implementation(libs.bundles.jline)
//                runtimeOnly(projects.lwjgl.launcher) {
//                    targetConfiguration = "launcher"
//                }
            }
        }
    }

//    mingwX64("windows")
//    linuxX64("linux")
//    macosX64("macos")
}

tasks {
//    assemble {
//        dependsOn(shadowJar)
//    }
//    nativeCompileClasspathJar {
//        dependsOn(shadowJar)
//        shadowJar.get().outputs.files.singleFile.apply { println(this) }
//        from(zipTree(shadowJar.map { it.outputs.files.singleFile }))
//        from(sourceSets.named("graal").map { it.output })
//        destinationDirectory = temporaryDir
//    }
//    shadowJar {
//        archiveClassifier = null
//        duplicatesStrategy = DuplicatesStrategy.FAIL
//    }
//    jar {
//        destinationDirectory = temporaryDir
//        manifest {
//            attributes("Implementation-Title" to "LWJGL GameLauncher")
//        }
//    }
//    run.configure {
//        outputs.upToDateWhen { false }
//        standardInput = System.`in`
//        standardOutput = System.out
//        errorOutput = System.err
//        workingDir = rootProject.mkdir("run")
//        mainClass = main
//    }
    withType<JavaExec>().configureEach {
        jvmArgs(defaultArgs)
        jvmArgs("--enable-preview")
    }
//    compileJava {
//        options.compilerArgs.add("--enable-preview")
//    }
//    test {
//        jvmArgs("--enable-preview")
//    }
}

logging.captureStandardOutput(LogLevel.WARN)

dependencies {
//    implementation(platform("org.lwjgl:lwjgl-bom:${lwjgl.version}"))
//    implementation("org.lwjgl:lwjgl")
//    implementation("org.lwjgl:lwjgl-glfw")
//    implementation("org.lwjgl:lwjgl-opengl")
//    implementation("org.lwjgl:lwjgl-opengles")
//    implementation("org.lwjgl:lwjgl-stb")
//
//    runtimeOnly("org.lwjgl", "lwjgl", classifier = lwjgl.natives)
//    runtimeOnly("org.lwjgl", "lwjgl-glfw", classifier = lwjgl.natives)
//    runtimeOnly("org.lwjgl", "lwjgl-opengl", classifier = lwjgl.natives)
//    runtimeOnly("org.lwjgl", "lwjgl-opengles", classifier = lwjgl.natives)
//    runtimeOnly("org.lwjgl", "lwjgl-stb", classifier = lwjgl.natives)
//    implementation(libs.joml)
//
////    "graalImplementation"("org.graalvm.sdk:graal-sdk:24.1.0")
////    "graalCompileOnly"(sourceSets.main.map { it.output })
//
//    implementation(projects.common)
//    implementation(libs.bundles.logging.runtime)
//    implementation(libs.disruptor)
//    implementation(libs.bundles.jline)
//    runtimeOnly(projects.lwjgl.launcher) {
//        targetConfiguration = "launcher"
//    }
}
