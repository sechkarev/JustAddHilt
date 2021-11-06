package com.sechkarev.justaddhilt.usecase.generation

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import org.jetbrains.kotlin.idea.KotlinLanguage
import java.io.StringWriter

// todo: unite?
@Service
class GenerateKotlinApplicationFile(private val project: Project) {

    operator fun invoke(
        packageName: String,
        applicationName: String,
    ): PsiFile {
        val template = freeMarkerConfig.getTemplate("appFileTemplate.kt.ftl")
        val templateText = StringWriter().use { writer ->
            template.process(
                mapOf(
                    "packageName" to packageName,
                    "applicationName" to applicationName,
                ),
                writer
            )
            writer.buffer.toString()
        }
        return PsiFileFactory.getInstance(project).createFileFromText(
            "$applicationName.kt",
            KotlinLanguage.INSTANCE,
            templateText,
        )
    }
}