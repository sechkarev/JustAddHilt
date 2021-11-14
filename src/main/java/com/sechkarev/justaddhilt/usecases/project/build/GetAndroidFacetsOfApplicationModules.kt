package com.sechkarev.justaddhilt.usecases.project.build

import com.android.tools.idea.gradle.dsl.api.ext.GradlePropertyModel
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.jetbrains.android.facet.AndroidFacet

@Service
class GetAndroidFacetsOfApplicationModules(project: Project) {

    private val getAllBuildModels = project.service<GetBuildModels>()

    operator fun invoke() = getAllBuildModels()
        .filter { moduleBuildModel ->
            moduleBuildModel.plugins().any { plugin ->
                plugin.name().getValue(GradlePropertyModel.STRING_TYPE)?.equals("com.android.application") == true
            }
        }
        .mapNotNull { it.psiElement }
        .mapNotNull { AndroidFacet.getInstance(it) }

}