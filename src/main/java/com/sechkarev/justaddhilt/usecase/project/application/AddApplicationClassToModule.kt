package com.sechkarev.justaddhilt.usecase.project.application

import com.android.tools.idea.util.androidFacet
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.guessModuleDir
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiManager
import com.intellij.psi.XmlRecursiveElementVisitor
import com.intellij.psi.xml.XmlTag
import com.sechkarev.justaddhilt.usecase.generation.GenerateApplicationFile
import com.sechkarev.justaddhilt.usecase.project.manifest.GetApplicationNameFromManifest
import org.jetbrains.android.dom.manifest.AndroidManifestXmlFile
import org.jetbrains.android.dom.manifest.getPrimaryManifestXml

@Service
class AddApplicationClassToModule(private val module: Module) {

    private val generateApplicationFile = GenerateApplicationFile(module)

    operator fun invoke(packageName: String, newApplicationFileName: String) {
        val applicationFile = generateApplicationFile(
            packageName = packageName,
            applicationName = newApplicationFileName
        )
        getDirectoryForApplicationFile(packageName)?.add(applicationFile)
        module.androidFacet?.getPrimaryManifestXml()?.setApplicationName(".$newApplicationFileName")
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

    private fun AndroidManifestXmlFile.setApplicationName(name: String) {
        accept(object : XmlRecursiveElementVisitor() {
            override fun visitXmlTag(tag: XmlTag?) {
                super.visitXmlTag(tag)
                if ("application" != tag?.name) return
                tag.setAttribute("android:name", name)
            }
        })
    }
}