package com.sechkarev.justaddhilt.usecases.project.kotlin

import com.android.tools.idea.gradle.dsl.api.GradleBuildModel
import com.android.tools.idea.gradle.dsl.api.ProjectBuildModel
import com.android.tools.idea.gradle.dsl.api.ext.GradlePropertyModel
import com.intellij.openapi.components.service
import com.intellij.openapi.module.Module
import org.jetbrains.kotlin.idea.util.application.executeOnPooledThread
import org.jetbrains.kotlin.idea.util.application.runReadAction

class IsKotlinConfiguredInModule(private val module: Module) {

    private val isKotlinEnabledInProject = module.project.service<IsKotlinEnabledInProject>()

    operator fun invoke(): Boolean {
        if (!isKotlinEnabledInProject()) return false
        var moduleBuildModel: GradleBuildModel? = null
        executeOnPooledThread {
            moduleBuildModel = runReadAction {
                ProjectBuildModel
                    .get(module.project)
                    .getModuleBuildModel(module)
            }
        }.get()
        return moduleBuildModel
            ?.plugins()
            ?.any {
                it.name().getValue(GradlePropertyModel.STRING_TYPE)?.equals("kotlin-android") == true
            }
            ?: false
    }
}