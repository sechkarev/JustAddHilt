package com.sechkarev.justaddhilt.usecases.project.kotlin

import com.android.tools.idea.gradle.dsl.api.ProjectBuildModel
import com.android.tools.idea.gradle.dsl.api.ext.GradlePropertyModel
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.module.Module

class IsKotlinConfiguredInModule(private val module: Module) {

    private val isKotlinEnabledInProject = module.project.service<IsKotlinEnabledInProject>()

    operator fun invoke(): Boolean {
        if (!isKotlinEnabledInProject()) return false
        return ProjectBuildModel
            .get(module.project)
            .getModuleBuildModel(module)
            ?.plugins()
            ?.any {
                it.name().getValue(GradlePropertyModel.STRING_TYPE)?.equals("kotlin-android") == true
            }
            ?: false
    }
}