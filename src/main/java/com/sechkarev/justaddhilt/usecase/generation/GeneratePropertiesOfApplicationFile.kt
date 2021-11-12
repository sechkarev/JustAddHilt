package com.sechkarev.justaddhilt.usecase.generation

import com.intellij.openapi.components.Service
import com.intellij.openapi.module.Module
import com.intellij.util.IncorrectOperationException
import com.sechkarev.justaddhilt.generation.ApplicationFileLanguage
import com.sechkarev.justaddhilt.generation.ApplicationFileProperties
import com.sechkarev.justaddhilt.usecase.project.kotlin.IsKotlinConfiguredInModule

@Service
class GeneratePropertiesOfApplicationFile(module: Module) {

    private val getDirectoryForApplicationFile = GetDirectoryForGeneratingApplicationFile(module)
    private val kotlinConfiguredInModule = IsKotlinConfiguredInModule(module)

    operator fun invoke(): ApplicationFileProperties {
        val defaultFileName = "CustomApplication"
        val language = if (kotlinConfiguredInModule()) ApplicationFileLanguage.KOTLIN else ApplicationFileLanguage.JAVA
        return ApplicationFileProperties(
            findAvailableFileName(1, defaultFileName, language.extension),
            language,
        )
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
}