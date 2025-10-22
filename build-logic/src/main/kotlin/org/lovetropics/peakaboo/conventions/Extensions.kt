package org.lovetropics.peakaboo.conventions

import net.neoforged.moddevgradle.dsl.NeoForgeExtension
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.named

internal val ExtensionContainer.conventions: ConventionsExtension
    get() = this.getByName<ConventionsExtension>(ConventionsExtension.EXTENSION_NAME)

internal val ExtensionContainer.neoForge: NeoForgeExtension
    get() = this.getByName<NeoForgeExtension>(NeoForgeExtension.NAME)

internal val Project.sourceSets: SourceSetContainer
    get() = this.extensions.getByName<SourceSetContainer>("sourceSets")

internal val SourceSetContainer.main: NamedDomainObjectProvider<SourceSet>
    get() = this.named<SourceSet>(SourceSet.MAIN_SOURCE_SET_NAME)
