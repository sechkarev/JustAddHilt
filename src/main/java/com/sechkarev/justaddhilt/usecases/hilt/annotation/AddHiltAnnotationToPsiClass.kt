package com.sechkarev.justaddhilt.usecases.hilt.annotation

import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.util.concurrency.AppExecutorUtil
import org.jetbrains.kotlin.idea.KotlinLanguage

@Service
class AddHiltAnnotationToPsiClass(private val project: Project) {

    private val addHiltAnnotationToJavaClass = project.service<AddHiltAnnotationToJavaClass>()
    private val addHiltAnnotationToKotlinClass = project.service<AddHiltAnnotationToKotlinClass>()

    operator fun invoke(psiClass: PsiClass): Boolean {
        if (psiClassHasHiltAnnotation(psiClass)) return false
        if (psiClass.language is JavaLanguage) {
            addHiltAnnotationToJavaClass(psiClass)
        } else if (psiClass.language is KotlinLanguage) {
            addHiltAnnotationToKotlinClass(psiClass)
        }
        return true
    }

    private fun psiClassHasHiltAnnotation(psiClass: PsiClass) = ReadAction.nonBlocking<Boolean> {
        if (project.isDisposed) {
            false
        } else {
            psiClass.hasAnnotation("dagger.hilt.android.HiltAndroidApp")
        }
    }.expireWith(project)
        .submit(AppExecutorUtil.getAppExecutorService())
        .get()
}