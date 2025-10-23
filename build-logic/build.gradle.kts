plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    maven("https://maven.neoforged.net/releases")
}

dependencies {
    implementation("net.neoforged.moddev:net.neoforged.moddev.gradle.plugin:2.0.115")
}
