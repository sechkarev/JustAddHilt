package com.sechkarev.justaddhilt.usecase.generation

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.guessModuleDir
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiManager
import com.sechkarev.justaddhilt.usecase.project.IsKotlinEnabledInProject

@Service
class GenerateApplicationFile(private val module: Module) {

    private val kotlinEnabledInProject = module.project.service<IsKotlinEnabledInProject>()
    private val generateKotlinApplicationFile = module.project.service<GenerateKotlinApplicationFile>()
    private val generateJavaApplicationFile = module.project.service<GenerateJavaApplicationFile>()

    operator fun invoke(
        packageName: String,
        applicationName: String,
    ) {
        getDirectoryForApplicationFile(packageName)?.add(generateApplicationFile(packageName, applicationName))
    }

    private fun generateApplicationFile(
        packageName: String,
        applicationName: String,
    ) = if (kotlinEnabledInProject()) {
        generateKotlinApplicationFile(packageName, applicationName)
    } else {
        generateJavaApplicationFile(packageName, applicationName)
    }

    private fun getDirectoryForApplicationFile(packageName: String): PsiDirectory? {
        val moduleDir = module.guessModuleDir() ?: return null
        var result = PsiManager.getInstance(module.project).findDirectory(moduleDir)
            ?.findSubdirectory("src")
            ?.findSubdirectory("main")
            ?.let { it.findSubdirectory("kotlin") ?: it.findSubdirectory("java") }
        packageName.split('.').forEach {
            result = result?.findSubdirectory(it)
        }
        return result
    }

}