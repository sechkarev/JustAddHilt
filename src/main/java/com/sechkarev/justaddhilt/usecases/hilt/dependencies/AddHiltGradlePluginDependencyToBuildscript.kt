package com.sechkarev.justaddhilt.usecases.hilt.dependencies

import com.android.tools.idea.gradle.dsl.api.ProjectBuildModel
import com.intellij.openapi.command.executeCommand
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.psi.codeStyle.CodeStyleManager
import com.sechkarev.justaddhilt.usecases.hilt.version.GetHiltVersion
import org.jetbrains.kotlin.idea.util.application.runWriteAction

@Service
class AddHiltGradlePluginDependencyToBuildscript(private val project: Project) {

    private val getHiltVersion = project.service<GetHiltVersion>()

    operator fun invoke(): Boolean {
        val projectBuildModel = ProjectBuildModel.get(project).projectBuildModel ?: return false
        val dependencies = projectBuildModel.buildscript().dependencies()
        val hiltGradlePluginAlreadyPresent = dependencies.artifacts().any {
            it.name().toString() == hiltPluginName && it.group().toString() == hiltPluginGroup
        }
        if (hiltGradlePluginAlreadyPresent) {
            return false
        }
        executeCommand(project, "Add Hilt Dependency to Project-Wide Gradle Script") {
            runWriteAction {
                dependencies.addArtifact(
                    configurationName,
                    "$hiltPluginGroup:$hiltPluginName:${getHiltVersion()}"
                )
                projectBuildModel.applyChanges()
                projectBuildModel
                    .buildscript()
                    .dependencies()
                    .psiElement
                    ?.let { CodeStyleManager.getInstance(project).reformat(it) }
            }
        }
        return true
    }

    private companion object {
        const val configurationName = "classpath"
        const val hiltPluginGroup = "com.google.dagger"
        const val hiltPluginName = "hilt-android-gradle-plugin"
    }
}