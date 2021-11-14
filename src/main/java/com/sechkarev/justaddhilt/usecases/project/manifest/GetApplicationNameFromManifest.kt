package com.sechkarev.justaddhilt.usecases.project.manifest

import com.android.SdkConstants
import com.android.tools.idea.util.androidFacet
import com.intellij.openapi.components.Service
import com.intellij.openapi.module.Module
import com.intellij.psi.XmlRecursiveElementVisitor
import com.intellij.psi.xml.XmlTag
import org.jetbrains.android.dom.manifest.AndroidManifestXmlFile
import org.jetbrains.android.dom.manifest.getPrimaryManifestXml

class GetApplicationNameFromManifest(private val module: Module) {

    operator fun invoke() = module.androidFacet?.getPrimaryManifestXml()?.findApplicationName()

    private fun AndroidManifestXmlFile.findApplicationName(): String? {
        var result: String? = null
        accept(
            object : XmlRecursiveElementVisitor() {
                override fun visitXmlTag(tag: XmlTag?) {
                    super.visitXmlTag(tag)
                    if ("application" != tag?.name) return
                    tag.getAttributeValue(SdkConstants.ATTR_NAME, SdkConstants.ANDROID_URI)?.let { result = it }
                }
            }
        )
        return result
    }

}