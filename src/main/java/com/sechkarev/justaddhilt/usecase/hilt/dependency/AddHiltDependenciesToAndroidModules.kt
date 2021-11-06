package com.sechkarev.justaddhilt.usecase.hilt.dependency

import com.android.tools.idea.gradle.dsl.api.PluginModel
import com.android.tools.idea.gradle.dsl.api.dependencies.ArtifactDependencyModel
import com.android.tools.idea.gradle.dsl.api.dependencies.DependenciesModel
import com.intellij.openapi.command.executeCommand
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.psi.codeStyle.CodeStyleManager
import com.sechkarev.justaddhilt.usecase.project.build.GetBuildModelsWithAndroidFacet
import com.sechkarev.justaddhilt.usecase.hilt.version.GetHiltVersion
import com.sechkarev.justaddhilt.getGroupName
import org.jetbrains.kotlin.idea.util.application.runWriteAction

@Service
class AddHiltDependenciesToAndroidModules(private val project: Project) {

    private val getHiltVersion = project.service<GetHiltVersion>()
    private val getAllBuildModelsWithAndroidFacet = project.service<GetBuildModelsWithAndroidFacet>()

    operator fun invoke() {
        getAllBuildModelsWithAndroidFacet().forEach { moduleBuildModel ->
            val pluginNames = PluginModel.extractNames(moduleBuildModel.plugins())
            val kaptPluginEnabled = pluginNames.any { it == "kotlin-kapt" }
            val hiltPluginEnabled = pluginNames.any { it == "dagger.hilt.android.plugin" }
            if (!hiltPluginEnabled) {
                moduleBuildModel.applyPlugin("dagger.hilt.android.plugin")
            }
            val hiltVersion = getHiltVersion()
            if (!isDependencyExist(moduleBuildModel.dependencies(), hiltImplementationDependencyName)) {
                moduleBuildModel.dependencies().addArtifact(
                    "implementation",
                    "$hiltImplementationDependencyName:$hiltVersion"
                )
            }
            if (!isDependencyExist(moduleBuildModel.dependencies(), hiltKaptDependencyName)) {
                moduleBuildModel.dependencies().addArtifact(
                    if (kaptPluginEnabled) "kapt" else "annotationProcessor",
                    "$hiltKaptDependencyName:$hiltVersion"
                )
            }
            // todo: add test dependencies (do we even need to?)
            moduleBuildModel.applyChanges()
            moduleBuildModel.psiElement?.let { psiElement -> CodeStyleManager.getInstance(project).reformat(psiElement) }
        }
    }

    private fun isDependencyExist(dependenciesModel: DependenciesModel, dependencyName: String) = dependenciesModel
        .all()
        .any { it is ArtifactDependencyModel && it.getGroupName() == dependencyName }

    private companion object {
        const val hiltImplementationDependencyName = "com.google.dagger:hilt-android"
        const val hiltKaptDependencyName = "com.google.dagger:hilt-compiler"
    }
}