package com.sechkarev.justaddhilt.usecases.project.kotlin

import com.android.tools.idea.gradle.dsl.api.ProjectBuildModel
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.sechkarev.justaddhilt.getGroupName

@Service
class IsKotlinEnabledInProject(private val project: Project) {
    operator fun invoke() = ProjectBuildModel
        .get(project)
        .projectBuildModel
        ?.buildscript()
        ?.dependencies()
        ?.artifacts("classpath")
        ?.any { it.getGroupName() == "org.jetbrains.kotlin:kotlin-gradle-plugin" } ?: false
}