plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
}

configurations.consumable("launcher") {
    outgoing.artifact(tasks.jar) {
        name = "lwjgl-launcher"
    }
}
