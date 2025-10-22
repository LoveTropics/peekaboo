package org.lovetropics.peakaboo.conventions

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register
import org.gradle.language.jvm.tasks.ProcessResources
import javax.inject.Inject

abstract class ConventionsExtension @Inject constructor(private val project: Project) {

    companion object {
        const val EXTENSION_NAME = "conventions"
    }

    abstract val javaVersion: Property<Int>

    fun createModMetadataTask(replaceProperties: Map<String, String>): TaskProvider<ProcessResources> {
        return createModMetadataTask("generateModMetadata", replaceProperties)
    }

    fun createModMetadataTask(name: String, replaceProperties: Map<String, String>): TaskProvider<ProcessResources> {
        val task = project.tasks.register<ProcessResources>(name) {
            from(project.file("src/main/templates"))
            into(project.layout.buildDirectory.dir("generated/sources/modMetadata"))
            expand(replaceProperties)
        }

        project.sourceSets.main.configure {
            resources.srcDir(task)
        }
        project.extensions.neoForge.ideSyncTask(task)

        return task
    }
}
