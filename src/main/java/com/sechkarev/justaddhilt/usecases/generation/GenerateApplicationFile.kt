package com.sechkarev.justaddhilt.usecases.generation

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.sechkarev.justaddhilt.generation.ApplicationFileProperties
import com.sechkarev.justaddhilt.generation.freeMarkerConfig
import java.io.StringWriter

@Service
class GenerateApplicationFile(private val project: Project) {

    operator fun invoke(packageName: String, applicationFileProperties: ApplicationFileProperties): PsiFile {
        val template = freeMarkerConfig.getTemplate(applicationFileProperties.language.templateName)
        val templateText = StringWriter().use { writer ->
            template.process(
                mapOf(
                    "packageName" to packageName,
                    "applicationName" to applicationFileProperties.name,
                ),
                writer
            )
            writer.buffer.toString()
        }
        return PsiFileFactory.getInstance(project).createFileFromText(
            "${applicationFileProperties.name}.${applicationFileProperties.language.extension}",
            applicationFileProperties.language.language,
            templateText,
        )
    }
}