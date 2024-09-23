plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin.jvmToolchain(21)

dependencies {
    api(libs.bundles.logging)

    testImplementation(libs.junit.jupiter.api)
    testImplementation(kotlin("test"))
    testRuntimeOnly(libs.junit.jupiter.engine)
}

tasks.test {
    useJUnitPlatform()
}
