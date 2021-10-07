package com.sechkarev.justaddhilt

import com.android.tools.idea.gradle.dsl.api.ProjectBuildModel
import com.android.tools.idea.gradle.dsl.model.repositories.MavenCentralRepositoryModel
import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project

class AddHiltAction : AnAction() {

    private val logger = logger<AddHiltAction>()

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val projectBuildModel = ProjectBuildModel.get(project).projectBuildModel // todo: what does this mean if this is null?
        val mavenCentralPresent = projectBuildModel
            ?.repositories()
            ?.containsMethodCall(MavenCentralRepositoryModel.MAVEN_CENTRAL_METHOD_NAME)

        val mavenCentralPresentString = "Maven Central is present in repos = $mavenCentralPresent"

        logger.debug(mavenCentralPresentString)

        showNotification(project, mavenCentralPresentString)
    }

    private fun showNotification(project: Project, text: String = "My Message") {
        val noti = NotificationGroup("myplugin", NotificationDisplayType.BALLOON, true)
        noti.createNotification(
            title = "Just add Hilt",
            content = text,
            type = NotificationType.INFORMATION,
            listener = null,
        ).notify(project)
    }
}