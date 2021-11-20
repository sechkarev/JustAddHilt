package com.sechkarev.justaddhilt.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.sechkarev.justaddhilt.notifications.ShowHiltAlreadyPresentNotification
import com.sechkarev.justaddhilt.notifications.ShowHiltWasAddedNotification
import com.sechkarev.justaddhilt.notifications.ShowNoAndroidModulesNotification
import com.sechkarev.justaddhilt.usecases.hilt.annotation.AddHiltAnnotationToApplicationClasses
import com.sechkarev.justaddhilt.usecases.hilt.dependencies.AddHiltDependenciesToAndroidModules
import com.sechkarev.justaddhilt.usecases.hilt.dependencies.AddHiltGradlePluginDependencyToBuildscript
import com.sechkarev.justaddhilt.usecases.project.build.AreAndroidModulesPresentInProject
import com.sechkarev.justaddhilt.usecases.project.build.SyncProjectWithGradleFiles

class AddHiltAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        if (!project.service<AreAndroidModulesPresentInProject>()()) {
            project.service<ShowNoAndroidModulesNotification>()()
            return
        }

        val buildscriptGradlePluginWasAdded = project.service<AddHiltGradlePluginDependencyToBuildscript>()()
        val hiltDependenciesWereAdded = project.service<AddHiltDependenciesToAndroidModules>()()
        if (buildscriptGradlePluginWasAdded || hiltDependenciesWereAdded) {
            project.service<SyncProjectWithGradleFiles>()(
                onSyncFinished = {
                    addHiltAnnotationToApplicationClasses(project)
                }
            )
        } else {
            addHiltAnnotationToApplicationClasses(project)
        }
    }

    private fun addHiltAnnotationToApplicationClasses(project: Project) {
        val codeWasAdded = project.service<AddHiltAnnotationToApplicationClasses>()()
        if (codeWasAdded) {
            project.service<ShowHiltWasAddedNotification>()()
        } else {
            project.service<ShowHiltAlreadyPresentNotification>()()
        }
    }
}
