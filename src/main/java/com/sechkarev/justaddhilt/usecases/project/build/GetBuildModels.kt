package com.sechkarev.justaddhilt.usecases.project.build

import com.android.tools.idea.gradle.dsl.api.GradleBuildModel
import com.android.tools.idea.gradle.dsl.api.ProjectBuildModel
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project

@Service
class GetBuildModels(private val project: Project) {
    operator fun invoke(): List<GradleBuildModel> = ProjectBuildModel
        .get(project)
        .allIncludedBuildModels
}