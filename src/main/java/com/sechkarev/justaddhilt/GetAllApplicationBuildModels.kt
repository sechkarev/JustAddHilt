package com.sechkarev.justaddhilt

import com.android.tools.idea.gradle.dsl.api.ext.GradlePropertyModel
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

@Service
class GetAllApplicationBuildModels(private val project: Project) {

    private val getAllBuildModels = project.service<GetAllBuildModels>()

    operator fun invoke() = getAllBuildModels()
        .filter { moduleBuildModel ->
            moduleBuildModel.plugins().any { plugin ->
                plugin.name().getValue(GradlePropertyModel.STRING_TYPE)?.equals("com.android.application") == true
            }
        }

}