package com.sechkarev.justaddhilt.usecase.generation

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.module.Module
import com.sechkarev.justaddhilt.usecase.project.IsKotlinEnabledInProject

@Service
class GenerateApplicationFile(module: Module) {

    private val kotlinEnabledInProject = module.project.service<IsKotlinEnabledInProject>()
    private val generateKotlinApplicationFile = module.project.service<GenerateKotlinApplicationFile>()
    private val generateJavaApplicationFile = module.project.service<GenerateJavaApplicationFile>()

    operator fun invoke(
        packageName: String,
        applicationName: String,
    ) = if (kotlinEnabledInProject()) {
        generateKotlinApplicationFile(packageName, applicationName)
    } else {
        generateJavaApplicationFile(packageName, applicationName)
    }
}