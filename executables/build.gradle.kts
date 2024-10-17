import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import de.dasbabypixel.gamelauncher.gradle.*
import org.graalvm.buildtools.gradle.tasks.BuildNativeImageTask

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.graal.native)
}

val sourceLauncher = sourceSets.register("launcher")
val sourceGraal = sourceSets.register("graal")

kotlin {
    jvmToolchain(22)
}

dependencies {
    "launcherImplementation"(projects.engine)

    "graalImplementation"("org.graalvm.sdk:graal-sdk:24.1.0")
}

graalvmNative {
    toolchainDetection = true

    binaries.remove(binaries.getByName("main"))
    binaries.remove(binaries.getByName("test"))
    binaries {
        register("prod") {
            javaLauncher = javaToolchains.launcherFor {
                languageVersion = JavaLanguageVersion.of(23)
                vendor = JvmVendorSpec.GRAAL_VM
            }
            imageName = "engine"
            mainClass = lwjglMain
            runtimeArgs(lwjglDefaultArgs)
        }
        register("dev") {
            javaLauncher = javaToolchains.launcherFor {
                languageVersion = JavaLanguageVersion.of(23)
                vendor = JvmVendorSpec.GRAAL_VM
            }
            imageName = "engine"
            mainClass = lwjglMain
            runtimeArgs(lwjglDefaultDevArgs)
        }
        forEach {
            println(it.name)
        }
    }
}

tasks {
    val genProdArgs = register<JvmArgumentGenerator>("genProdArgs") {
        arguments.addAll(lwjglDefaultArgs)
    }
    val genDevArgs = register<JvmArgumentGenerator>("genDevArgs") {
        arguments.addAll(lwjglDefaultDevArgs)
    }
    named("nativeBuild") { enabled = false }
    named("nativeCompile") { enabled = false }
    register("nativeAssemble") {
        dependsOn(named("nativeDevCompile"))
        dependsOn(named("nativeProdCompile"))
    }
    jar { enabled = false }
    val lwjglDev = register<ShadowJar>("lwjgl-dev") {
        group = launcherGroup
        from(sourceLauncher.map { it.output })
        archiveClassifier = "dev"
        configurations.add(project.configurations.getByName("launcherRuntimeClasspath"))
        from(genDevArgs) { rename { "gamelauncher.jvmargs" } }
        manifest {
            attributes["Main-Class"] = lwjglLauncherMain
        }
    }
    val lwjglProd = register<ShadowJar>("lwjgl-prod") {
        group = launcherGroup
        from(sourceLauncher.map { it.output })
        archiveClassifier = "prod"
        configurations.add(project.configurations.getByName("launcherRuntimeClasspath"))
        from(genProdArgs) { rename { "gamelauncher.jvmargs" } }
        manifest {
            attributes["Main-Class"] = lwjglLauncherMain
        }
    }
    val lwjglGraalProd = register<ShadowJar>("graal-lwjgl-prod") {
        from(lwjglProd)
        from(sourceGraal.map { it.output })
        destinationDirectory = temporaryDir
    }
    val lwjglGraalDev = register<ShadowJar>("graal-lwjgl-dev") {
        from(lwjglDev)
        from(sourceGraal.map { it.output })
        destinationDirectory = temporaryDir
    }
    assemble {
        dependsOn(lwjglDev.get())
        dependsOn(lwjglProd.get())
    }
    register<JavaExec>("graalRunDevWithAgent") {
        outputs.upToDateWhen { false }
        workingDir(rootProject.file("run"))
        doFirst { workingDir.mkdirs() }
        classpath(lwjglDev)
        javaLauncher = project.javaToolchains.launcherFor {
            languageVersion = JavaLanguageVersion.of(23)
            vendor = JvmVendorSpec.GRAAL_VM
        }
        mainClass = lwjglMain
        jvmArgs(lwjglDefaultDevArgs)
        val configDir = project.file("src/graal/resources/META-INF/native-image/de.dasbabypixel.gamelauncher.lwjgl/generated").absolutePath
        jvmArgs("-agentlib:native-image-agent=config-merge-dir=$configDir,config-write-period-secs=15")
        standardInput = System.`in`
        standardOutput = System.out
        errorOutput = System.err
    }

    afterEvaluate {
        named<BuildNativeImageTask>("nativeProdCompile") {
            dependsOn(lwjglGraalProd.get())
            classpathJar.set(lwjglGraalProd.get().archiveFile)
        }
        named<BuildNativeImageTask>("nativeDevCompile") {
            dependsOn(lwjglGraalDev)
            classpathJar.set(lwjglGraalDev.get().archiveFile)
        }
        named("generateDevResourcesConfigFile") {
            dependsOn(lwjglGraalDev)
        }
    }
}
