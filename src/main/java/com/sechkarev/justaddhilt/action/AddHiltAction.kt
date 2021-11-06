package com.sechkarev.justaddhilt.action

import com.android.tools.idea.projectsystem.ProjectSystemSyncManager
import com.android.tools.idea.projectsystem.gradle.GradleProjectSystemSyncManager
import com.android.tools.idea.util.androidFacet
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.executeCommand
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.sechkarev.justaddhilt.usecase.hilt.annotation.AddHiltAnnotationToPsiClass
import com.sechkarev.justaddhilt.usecase.hilt.dependency.AddHiltDependenciesToAndroidModules
import com.sechkarev.justaddhilt.usecase.project.IsKotlinEnabledInProject
import com.sechkarev.justaddhilt.usecase.project.application.AddApplicationClassToModule
import com.sechkarev.justaddhilt.usecase.project.application.IsApplicationClassGenerationRequiredForModule
import com.sechkarev.justaddhilt.usecase.project.application.GetModuleApplicationClass
import com.sechkarev.justaddhilt.usecase.project.build.GetAndroidFacetsOfApplicationModules
import com.sechkarev.justaddhilt.usecase.project.build.GetBuildModels
import com.sechkarev.justaddhilt.usecase.project.repository.EnsureMavenCentralRepositoryPresent
import org.jetbrains.android.dom.manifest.getPrimaryManifestXml
import org.jetbrains.kotlin.idea.util.application.runWriteAction

class AddHiltAction : AnAction() {

    private val logger = logger<AddHiltAction>()

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return // todo: show error if this is null

        var mavenRepositoryWasAdded: Boolean

        executeCommand {
            runWriteAction {
                mavenRepositoryWasAdded = project.service<EnsureMavenCentralRepositoryPresent>()()
            }
        }

        val buildModels = project.service<GetBuildModels>()()
        logger.warn("build models: ${buildModels.joinToString { it.moduleRootDirectory.name }}")
        // todo: show error if this list is empty

        executeCommand {
            runWriteAction {
                project.service<AddHiltDependenciesToAndroidModules>()()
            }
        }

        //todo: refresh gradle only if repos/dependencies were added
        val listenableFuture = GradleProjectSystemSyncManager(project).syncProject(ProjectSystemSyncManager.SyncReason.PROJECT_MODIFIED)
        listenableFuture.addListener(
            { addAnnotationToApplicationClasses(project) },
            { DumbService.getInstance(project).smartInvokeLater(it) },
        )
    }

    private fun addAnnotationToApplicationClasses(project: Project) {
        // todo: show something if everything was already added
        val facetsOfApplicationModules = project.service<GetAndroidFacetsOfApplicationModules>()
        executeCommand {
            runWriteAction {
                facetsOfApplicationModules()
                    .map { it.module }
                    .forEach { module ->
                        val packageDirectoryName = module.androidFacet?.getPrimaryManifestXml()?.packageName ?: return@forEach
                        val shouldGenerateApplicationClass = IsApplicationClassGenerationRequiredForModule(module)
                        if (shouldGenerateApplicationClass()) {
                            logger.warn("generating app class for module ${module.name}")
                            val newFileName = "GeneratedApplication" // todo: what if it already exists?
                            val addApplicationFileToModule = AddApplicationClassToModule(module)
                            addApplicationFileToModule(packageDirectoryName, newFileName)
                        } else {
                            logger.warn("adding annotation to app class in module ${module.name}")
                            val getModuleApplicationClass = GetModuleApplicationClass(module)
                            val applicationPsiClass = getModuleApplicationClass() ?: return@forEach
                            val addHiltAnnotationToPsiClass = project.service<AddHiltAnnotationToPsiClass>()
                            addHiltAnnotationToPsiClass(applicationPsiClass)
                        }
                    }
            }
        }
    }
}