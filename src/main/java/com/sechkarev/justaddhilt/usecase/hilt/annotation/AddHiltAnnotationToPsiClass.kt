package com.sechkarev.justaddhilt.usecase.hilt.annotation

import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import org.jetbrains.kotlin.idea.KotlinLanguage

@Service
class AddHiltAnnotationToPsiClass(private val project: Project) {

    private val addHiltAnnotationToJavaClass = project.service<AddHiltAnnotationToJavaClass>()
    private val addHiltAnnotationToKotlinClass = project.service<AddHiltAnnotationToKotlinClass>()

    operator fun invoke(psiClass: PsiClass) {
        val applicationClassHasHiltAnnotation = psiClass.hasAnnotation("dagger.hilt.android.HiltAndroidApp")
        if (applicationClassHasHiltAnnotation) return
        if (psiClass.language is JavaLanguage) {
            addHiltAnnotationToJavaClass(psiClass)
        } else if (psiClass.language is KotlinLanguage) {
            addHiltAnnotationToKotlinClass(psiClass)
        }
    }
}