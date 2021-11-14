package com.sechkarev.justaddhilt.usecases.generation

import com.android.tools.idea.util.androidFacet
import com.intellij.openapi.components.Service
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.guessModuleDir
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiManager
import org.jetbrains.android.dom.manifest.getPrimaryManifestXml

class GetDirectoryForGeneratingApplicationFile(private val module: Module) {

    operator fun invoke(): PsiDirectory? {
        val packageName = module.androidFacet?.getPrimaryManifestXml()?.packageName ?: ""
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