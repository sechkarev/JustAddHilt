package com.sechkarev.justaddhilt

import com.android.tools.idea.gradle.dsl.api.GradleBuildModel
import com.android.tools.idea.gradle.dsl.api.ProjectBuildModel
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import org.jetbrains.android.facet.AndroidFacet

@Service
class GetAllModulesWithAndroidFacet(private val project: Project) {
    operator fun invoke(): List<GradleBuildModel> {
        return ProjectBuildModel
            .get(project)
            .allIncludedBuildModels
            .filter { buildModel -> buildModel.psiElement?.let { AndroidFacet.getInstance(it) } != null }
    }
}
// todo: can I add other services to the ctor?