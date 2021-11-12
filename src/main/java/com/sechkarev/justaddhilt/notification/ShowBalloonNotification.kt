package com.sechkarev.justaddhilt.notification

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project

@Service
class ShowBalloonNotification(private val project: Project) {

    operator fun invoke(text: String, notificationType: NotificationType = NotificationType.INFORMATION) {
        NotificationGroupManager.getInstance().getNotificationGroup("Just Add Hilt Notification Group")
            .createNotification(text, notificationType)
            .notify(project)
    }
}