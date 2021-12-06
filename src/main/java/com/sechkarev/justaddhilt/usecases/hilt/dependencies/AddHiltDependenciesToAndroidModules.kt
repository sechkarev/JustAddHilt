package com.sechkarev.justaddhilt.usecases.hilt.dependencies

import com.android.tools.idea.gradle.dsl.api.GradleBuildModel
import com.android.tools.idea.gradle.dsl.api.PluginModel
import com.android.tools.idea.gradle.dsl.api.dependencies.ArtifactDependencyModel
import com.android.tools.idea.gradle.dsl.api.dependencies.DependenciesModel
import com.intellij.openapi.command.executeCommand
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.psi.codeStyle.CodeStyleManager
import com.sechkarev.justaddhilt.ext.getGroupName
import com.sechkarev.justaddhilt.usecases.hilt.version.GetHiltVersion
import com.sechkarev.justaddhilt.usecases.project.build.GetBuildModelsWithAndroidFacet
import org.jetbrains.kotlin.idea.util.application.runWriteAction

@Service
class AddHiltDependenciesToAndroidModules(private val project: Project) {

    private val getHiltVersion = project.service<GetHiltVersion>()
    private val getAllBuildModelsWithAndroidFacet = project.service<GetBuildModelsWithAndroidFacet>()

    operator fun invoke(): Boolean {
        var pluginWasAdded = false
        var dependenciesWereAdded = false
        getAllBuildModelsWithAndroidFacet().forEach { moduleBuildModel ->
            val pluginNames = PluginModel.extractNames(moduleBuildModel.plugins())
            val hiltPluginEnabled = pluginNames.any { it == hiltPluginName }
            if (!hiltPluginEnabled) {
                moduleBuildModel.applyPlugin(hiltPluginName)
                pluginWasAdded = true
            }
            val hiltVersion = getHiltVersion()
            if (!isDependencyExist(moduleBuildModel.dependencies(), hiltImplementationDependencyName)) {
                addHiltImplementationDependency(moduleBuildModel, hiltVersion)
                dependenciesWereAdded = true
            }
            val kaptPluginEnabled = pluginNames.any { it == kaptPluginName }
            if (!isDependencyExist(moduleBuildModel.dependencies(), hiltKaptDependencyName)) {
                addHiltKaptDependency(moduleBuildModel, kaptPluginEnabled, hiltVersion)
                dependenciesWereAdded = true
            }
            executeCommand {
                runWriteAction {
                    moduleBuildModel.applyChanges()
                    if (pluginWasAdded) {
                        moduleBuildModel
                            .pluginsPsiElement
                            ?.let { CodeStyleManager.getInstance(project).reformat(it) }
                    }
                    if (dependenciesWereAdded) {
                        moduleBuildModel
                            .dependencies()
                            .psiElement
                            ?.let { CodeStyleManager.getInstance(project).reformat(it) }
                    }
                }
            }
        }
        return pluginWasAdded || dependenciesWereAdded
    }

    private fun isDependencyExist(
        dependenciesModel: DependenciesModel,
        dependencyName: String,
    ) = dependenciesModel
        .all()
        .any { it is ArtifactDependencyModel && it.getGroupName() == dependencyName }

    private fun addHiltImplementationDependency(
        moduleBuildModel: GradleBuildModel,
        hiltVersion: String,
    ) {
        moduleBuildModel.dependencies().addArtifact(
            "implementation",
            "$hiltImplementationDependencyName:$hiltVersion"
        )
    }

    private fun addHiltKaptDependency(
        moduleBuildModel: GradleBuildModel,
        kaptPluginEnabled: Boolean,
        hiltVersion: String,
    ) {
        moduleBuildModel.dependencies().addArtifact(
            if (kaptPluginEnabled) "kapt" else "annotationProcessor",
            "$hiltKaptDependencyName:$hiltVersion"
        )
    }

    private companion object {
        const val kaptPluginName = "kotlin-kapt"
        const val hiltPluginName = "dagger.hilt.android.plugin"
        const val hiltImplementationDependencyName = "com.google.dagger:hilt-android"
        const val hiltKaptDependencyName = "com.google.dagger:hilt-compiler"
    }
}