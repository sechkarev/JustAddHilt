package com.sechkarev.justaddhilt.usecases.project.application

import com.android.tools.idea.util.androidFacet
import com.intellij.openapi.command.executeCommand
import com.intellij.openapi.module.Module
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiManager
import com.intellij.psi.util.ClassUtil
import com.sechkarev.justaddhilt.usecases.project.manifest.GetApplicationNameFromManifest
import org.jetbrains.android.dom.manifest.getPrimaryManifestXml
import org.jetbrains.kotlin.idea.util.application.runWriteAction

class GetModuleApplicationClass(private val module: Module) {

    private val getApplicationNameFromManifest = GetApplicationNameFromManifest(module)

    operator fun invoke(): PsiClass? {
        val primaryManifestXml = module.androidFacet?.getPrimaryManifestXml()
        val packageName = primaryManifestXml?.packageName
        val applicationName = getApplicationNameFromManifest()
        val fullClassName = packageName + applicationName
        var applicationClass: PsiClass? = null
        executeCommand {
            applicationClass = runWriteAction {
                ClassUtil.findPsiClass(PsiManager.getInstance(module.project), fullClassName)
            }
        }
        return applicationClass
    }
}