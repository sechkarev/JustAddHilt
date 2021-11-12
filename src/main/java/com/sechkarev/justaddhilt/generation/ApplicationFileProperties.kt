package com.sechkarev.justaddhilt.generation

import com.intellij.lang.Language
import com.intellij.lang.java.JavaLanguage
import org.jetbrains.kotlin.idea.KotlinLanguage

data class ApplicationFileProperties(
    val name: String,
    val language: ApplicationFileLanguage
)

enum class ApplicationFileLanguage(val extension: String, val language: Language, val templateName: String) {
    JAVA("java", JavaLanguage.INSTANCE, "appFileTemplate.java.ftl"),
    KOTLIN("kt", KotlinLanguage.INSTANCE, "appFileTemplate.kt.ftl"),
}