package com.sechkarev.justaddhilt.usecases.project.application

import com.android.tools.idea.gradle.dsl.api.GradleBuildModel
import com.android.tools.idea.gradle.dsl.api.ProjectBuildModel
import com.android.tools.idea.gradle.dsl.api.ext.GradlePropertyModel
import com.android.tools.idea.util.androidFacet
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.command.executeCommand
import com.intellij.openapi.components.service
import com.intellij.openapi.module.Module
import com.intellij.psi.XmlRecursiveElementVisitor
import com.intellij.psi.xml.XmlTag
import com.intellij.util.IncorrectOperationException
import com.intellij.util.concurrency.AppExecutorUtil
import com.sechkarev.justaddhilt.generation.ApplicationFileLanguage
import com.sechkarev.justaddhilt.generation.ApplicationFileProperties
import com.sechkarev.justaddhilt.usecases.generation.GenerateApplicationFile
import com.sechkarev.justaddhilt.usecases.generation.GetDirectoryForGeneratingApplicationFile
import com.sechkarev.justaddhilt.usecases.project.kotlin.IsKotlinEnabledInProject
import org.jetbrains.android.dom.manifest.AndroidManifestXmlFile
import org.jetbrains.android.dom.manifest.getPrimaryManifestXml
import org.jetbrains.kotlin.idea.util.application.runWriteAction

class AddApplicationClassToModule(private val module: Module) {

    private val generateApplicationFile = module.project.service<GenerateApplicationFile>()
    private val isKotlinEnabledInProject = module.project.service<IsKotlinEnabledInProject>()
    private val getDirectoryForApplicationFile = GetDirectoryForGeneratingApplicationFile(module)

    operator fun invoke(classWasGeneratedCallback: (Boolean) -> Unit) {
        val packageName = module.androidFacet?.getPrimaryManifestXml()?.packageName
        if (packageName == null) {
            classWasGeneratedCallback(false)
            return
        }
        generatePropertiesOfApplicationFile { applicationFileProperties ->
            val applicationFile = generateApplicationFile(packageName, applicationFileProperties)
            executeCommand(
                module.project,
                "Add Class ${applicationFileProperties.name} to Module ${module.name}",
            ) {
                runWriteAction {
                    getDirectoryForApplicationFile()?.add(applicationFile)
                    registerApplicationClassInManifest(applicationFileProperties.name)
                }
            }
            classWasGeneratedCallback(true)
        }
    }

    private fun generatePropertiesOfApplicationFile(resultCallback: (ApplicationFileProperties) -> Unit) {
        val defaultFileName = "CustomApplication"
        isKotlinConfiguredInModule {
            val language = if (it) ApplicationFileLanguage.KOTLIN else ApplicationFileLanguage.JAVA
            resultCallback(
                ApplicationFileProperties(
                    findAvailableFileName(1, defaultFileName, language.extension),
                    language
                )
            )
        }
    }

    private fun isKotlinConfiguredInModule(resultCallback: (Boolean) -> Unit) {
        if (!isKotlinEnabledInProject()) {
            resultCallback(false)
            return
        }
        ReadAction.nonBlocking<GradleBuildModel> {
            if (module.isDisposed || module.project.isDisposed) {
                null
            } else {
                ProjectBuildModel
                    .get(module.project)
                    .getModuleBuildModel(module)
            }
        }.expireWith(module.project)
            .finishOnUiThread(ModalityState.NON_MODAL) {
                resultCallback(it.hasKotlinConfigured)
            }
            .submit(AppExecutorUtil.getAppExecutorService())
    }

    private fun registerApplicationClassInManifest(name: String) {
        module.androidFacet?.getPrimaryManifestXml()?.setApplicationName(".$name")
    }

    private fun findAvailableFileName(attemptNumber: Int, fileName: String, extension: String): String {
        return try {
            val attemptedFileName = if (attemptNumber > 1) "$fileName$attemptNumber" else fileName
            val fullFileName = "$attemptedFileName.$extension"
            getDirectoryForApplicationFile()?.checkCreateFile(fullFileName)
            attemptedFileName
        } catch (e: IncorrectOperationException) {
            findAvailableFileName(attemptNumber + 1, fileName, extension)
        }
    }

    private val GradleBuildModel.hasKotlinConfigured
        get() = plugins().any { pluginModel ->
            pluginModel.name().getValue(GradlePropertyModel.STRING_TYPE)?.equals("kotlin-android") == true
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