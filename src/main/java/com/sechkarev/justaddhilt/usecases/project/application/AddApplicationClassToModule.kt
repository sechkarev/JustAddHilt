package com.sechkarev.justaddhilt.usecases.project.application

import com.android.tools.idea.util.androidFacet
import com.intellij.openapi.command.executeCommand
import com.intellij.openapi.components.service
import com.intellij.openapi.module.Module
import com.intellij.psi.XmlRecursiveElementVisitor
import com.intellij.psi.xml.XmlTag
import com.sechkarev.justaddhilt.usecases.generation.GenerateApplicationFile
import com.sechkarev.justaddhilt.usecases.generation.GeneratePropertiesOfApplicationFile
import com.sechkarev.justaddhilt.usecases.generation.GetDirectoryForGeneratingApplicationFile
import org.jetbrains.android.dom.manifest.AndroidManifestXmlFile
import org.jetbrains.android.dom.manifest.getPrimaryManifestXml
import org.jetbrains.kotlin.idea.util.application.runWriteAction

class AddApplicationClassToModule(private val module: Module) {

    private val generateApplicationFile = module.project.service<GenerateApplicationFile>()
    private val getDirectoryForApplicationFile = GetDirectoryForGeneratingApplicationFile(module)
    private val generatePropertiesOfApplicationFile = GeneratePropertiesOfApplicationFile(module)

    operator fun invoke(): Boolean {
        val packageName = module.androidFacet?.getPrimaryManifestXml()?.packageName ?: return false
        val applicationFileProperties = generatePropertiesOfApplicationFile()
        val applicationFile = generateApplicationFile(packageName, applicationFileProperties)
        executeCommand(module.project) {
            runWriteAction {
                getDirectoryForApplicationFile()?.add(applicationFile)
                registerApplicationClassInManifest(applicationFileProperties.name)
            }
        }
        return true
    }

    private fun registerApplicationClassInManifest(name: String) {
        module.androidFacet?.getPrimaryManifestXml()?.setApplicationName(".$name")
    }

    private fun AndroidManifestXmlFile.setApplicationName(name: String) {
        accept(
            object : XmlRecursiveElementVisitor() {
                override fun visitXmlTag(tag: XmlTag?) {
                    super.visitXmlTag(tag)
                    if ("application" != tag?.name) return
                    tag.setAttribute("android:name", name)
                }
            }
        )
    }
}