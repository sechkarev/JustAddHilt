package com.sechkarev.justaddhilt.usecases.project.kotlin

import com.android.tools.idea.gradle.dsl.api.GradleBuildModel
import com.android.tools.idea.gradle.dsl.api.ProjectBuildModel
import com.android.tools.idea.gradle.dsl.api.ext.GradlePropertyModel
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.components.service
import com.intellij.openapi.module.Module
import com.intellij.util.concurrency.AppExecutorUtil

class IsKotlinConfiguredInModule(private val module: Module) {

    private val isKotlinEnabledInProject = module.project.service<IsKotlinEnabledInProject>()

    operator fun invoke(): Boolean {
        if (!isKotlinEnabledInProject()) return false
        return ReadAction.nonBlocking<GradleBuildModel> {
            if (module.isDisposed || module.project.isDisposed) {
                null
            } else {
                ProjectBuildModel
                    .get(module.project)
                    .getModuleBuildModel(module)
            }
        }.expireWith(module.project)
            .submit(AppExecutorUtil.getAppExecutorService())
            .get()
            ?.let { gradleBuildModel ->
                gradleBuildModel.plugins().any { pluginModel ->
                    pluginModel.name().getValue(GradlePropertyModel.STRING_TYPE)?.equals("kotlin-android") == true
                }
            }
            ?: false
    }
}