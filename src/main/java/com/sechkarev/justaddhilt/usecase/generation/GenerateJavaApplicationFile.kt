package com.sechkarev.justaddhilt.usecase.generation

import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import java.io.StringWriter

@Service
class GenerateJavaApplicationFile(private val project: Project) {

    operator fun invoke(
        packageName: String,
        applicationName: String,
    ): PsiFile {
        val template = freeMarkerConfig.getTemplate("appFileTemplate.java.ftl")
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
            "$applicationName.java",
            JavaLanguage.INSTANCE,
            templateText,
        )
    }
}