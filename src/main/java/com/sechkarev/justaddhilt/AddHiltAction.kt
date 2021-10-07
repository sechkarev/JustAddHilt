package com.sechkarev.justaddhilt

import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.notification.impl.NotificationGroupEP
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class AddHiltAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val ep = NotificationGroupEP()
        NotificationGroupManager.getInstance()
        val noti = NotificationGroup("myplugin", NotificationDisplayType.BALLOON, true)
        noti.createNotification("My Title",
            "My Message",
            NotificationType.INFORMATION,
            null
        ).notify(e.project)
    }

}