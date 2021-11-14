package com.sechkarev.justaddhilt.notifications

import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

@Service
class ShowHiltAlreadyPresentNotification(project: Project) {

    private val showBalloonNotification = project.service<ShowBalloonNotification>()

    operator fun invoke() {
        showBalloonNotification(
            "No code was added: Hilt is already added to the project.",
            NotificationType.WARNING,
        )
    }
}