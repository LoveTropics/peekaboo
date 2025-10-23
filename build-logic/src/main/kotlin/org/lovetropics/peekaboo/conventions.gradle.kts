package org.lovetropics.peekaboo

import org.lovetropics.peekaboo.conventions.ConventionsExtension

plugins {
    idea
    `java-library`
    `maven-publish`
    id("net.neoforged.moddev")
}

val conventionsExtension = extensions.create(ConventionsExtension.EXTENSION_NAME, ConventionsExtension::class)

val modVersion = project.property("mod_version")!! as String
if (providers.environmentVariable("RELEASE").isPresent) {
    version = modVersion
} else {
    providers.environmentVariable("GITHUB_RUN_NUMBER").orNull?.let {
        version = "${modVersion}+${it}-gha"
    } ?: run {
        version = "${modVersion}+local"
    }
}

project.afterEvaluate {
    val javaVersion = conventionsExtension.javaVersion.get()

    tasks.withType<JavaCompile> {
        options.release = javaVersion
    }

    if (JavaVersion.current() < JavaVersion.toVersion(javaVersion)) {
        java {
            toolchain {
                languageVersion.assign(JavaLanguageVersion.of(javaVersion))
            }
        }
    }
}

java {
    withSourcesJar()
}

project.sourceSets.main.configure {
    resources.srcDir(layout.projectDirectory.dir("src/generated/resources"))
}

val mavenUrl = providers.environmentVariable("MAVEN_URL")
if (mavenUrl.isPresent) {
    publishing {
        repositories {
            maven(mavenUrl.get()) {
                credentials {
                    username = providers.environmentVariable("MAVEN_USERNAME").orNull
                    password = providers.environmentVariable("MAVEN_PASSWORD").orNull
                }
            }
        }
    }
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}
