package com.sechkarev.justaddhilt.usecase.project.application

import com.android.tools.idea.util.androidFacet
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.module.Module
import com.intellij.psi.XmlRecursiveElementVisitor
import com.intellij.psi.xml.XmlTag
import com.sechkarev.justaddhilt.usecase.generation.GenerateApplicationFile
import com.sechkarev.justaddhilt.usecase.project.manifest.GetApplicationNameFromManifest
import org.jetbrains.android.dom.manifest.AndroidManifestXmlFile
import org.jetbrains.android.dom.manifest.getPrimaryManifestXml

@Service
class AddApplicationClassToModule(private val module: Module) {

    private val getCurrentApplicationName = module.getService(GetApplicationNameFromManifest::class.java)
    private val generateApplicationFile = module.project.service<GenerateApplicationFile>()

    operator fun invoke(newApplicationFileName: String) {
        val androidFacet = module.androidFacet ?: return
        val primaryManifestXml = androidFacet.getPrimaryManifestXml()
        val packageName = primaryManifestXml?.packageName
        val currentApplicationName = getCurrentApplicationName()
        if (currentApplicationName == null && packageName != null) {
            generateApplicationFile(
                packageName = packageName,
                applicationName = newApplicationFileName
            )
            primaryManifestXml.setApplicationName(".$newApplicationFileName")
        }
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