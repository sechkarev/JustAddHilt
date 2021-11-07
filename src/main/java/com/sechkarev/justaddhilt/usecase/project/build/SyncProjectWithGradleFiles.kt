package com.sechkarev.justaddhilt.usecase.project.build

import com.android.tools.idea.projectsystem.ProjectSystemSyncManager
import com.android.tools.idea.projectsystem.gradle.GradleProjectSystemSyncManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project

@Service
class SyncProjectWithGradleFiles(private val project: Project) {

    operator fun invoke(onSyncFinished: () -> Unit) {
        val listenableFuture = GradleProjectSystemSyncManager(project)
            .syncProject(ProjectSystemSyncManager.SyncReason.PROJECT_MODIFIED)
        listenableFuture.addListener(
            { onSyncFinished() },
            { DumbService.getInstance(project).smartInvokeLater(it) },
        )
    }
}