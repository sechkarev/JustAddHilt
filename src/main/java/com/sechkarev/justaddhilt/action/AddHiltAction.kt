package com.sechkarev.justaddhilt.action

import com.android.tools.idea.projectsystem.ProjectSystemSyncManager
import com.android.tools.idea.projectsystem.gradle.GradleProjectSystemSyncManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.executeCommand
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.sechkarev.justaddhilt.notification.ShowBalloonNotification
import com.sechkarev.justaddhilt.usecase.hilt.annotation.AddHiltAnnotationToPsiClass
import com.sechkarev.justaddhilt.usecase.hilt.dependency.AddHiltDependenciesToAndroidModules
import com.sechkarev.justaddhilt.usecase.project.application.AddApplicationClassToModule
import com.sechkarev.justaddhilt.usecase.project.application.IsApplicationClassGenerationRequiredForModule
import com.sechkarev.justaddhilt.usecase.project.application.GetModuleApplicationClass
import com.sechkarev.justaddhilt.usecase.project.build.GetAndroidFacetsOfApplicationModules
import com.sechkarev.justaddhilt.usecase.project.build.GetBuildModelsWithAndroidFacet
import org.jetbrains.kotlin.idea.util.application.runWriteAction

class AddHiltAction : AnAction() {

    private val logger = logger<AddHiltAction>()

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val buildModelsWithAndroidFacet = project.service<GetBuildModelsWithAndroidFacet>()()
        if (buildModelsWithAndroidFacet.isEmpty()) {
            project.service<ShowBalloonNotification>()(
                "Looks like this is not an Android project. Hilt can't be added to it."
            ) // todo: figure out how localization works here
        }

        var dependenciesWereAdded = false
        executeCommand {
            runWriteAction {
                dependenciesWereAdded = project.service<AddHiltDependenciesToAndroidModules>()(logger)
            }
        }

        if (dependenciesWereAdded) {
            syncProjectWithGradleFiles(
                project,
                this::addHiltAnnotationToApplicationClasses,
            )
        } else {
            addHiltAnnotationToApplicationClasses(project)
        }
    }

    private fun syncProjectWithGradleFiles(
        project: Project,
        onSyncFinished: (Project) -> Unit,
    ) {
        val listenableFuture = GradleProjectSystemSyncManager(project)
            .syncProject(ProjectSystemSyncManager.SyncReason.PROJECT_MODIFIED)
        listenableFuture.addListener(
            { onSyncFinished(project) },
            { DumbService.getInstance(project).smartInvokeLater(it) },
        )
    }

    private fun addHiltAnnotationToApplicationClasses(project: Project) {
        var codeWasAdded = false
        executeCommand {
            runWriteAction {
                project
                    .service<GetAndroidFacetsOfApplicationModules>()()
                    .map { it.module }
                    .forEach { module ->
                        val shouldGenerateApplicationClass = IsApplicationClassGenerationRequiredForModule(module)()
                        if (shouldGenerateApplicationClass) {
                            val appClassWasGenerated = AddApplicationClassToModule(module)()
                            codeWasAdded = codeWasAdded || appClassWasGenerated
                            logger.warn("app class was generated = $appClassWasGenerated for module = ${module.name}")
                        } else {
                            val applicationPsiClass = GetModuleApplicationClass(module)() ?: return@forEach
                            val annotationWasAdded = project.service<AddHiltAnnotationToPsiClass>()(applicationPsiClass)
                            codeWasAdded = codeWasAdded || annotationWasAdded
                            logger.warn("annotation was added = $annotationWasAdded to app class in module = ${module.name}")
                        }
                    }
                project.service<ShowBalloonNotification>()(
                    if (codeWasAdded) {
                        "Hilt was successfully added to the project."
                    } else {
                        "Hilt is already added to the project."
                    }
                )
            }
        }
    }
}
// todo: finish use case extraction

// todo: booleans look a little ugly