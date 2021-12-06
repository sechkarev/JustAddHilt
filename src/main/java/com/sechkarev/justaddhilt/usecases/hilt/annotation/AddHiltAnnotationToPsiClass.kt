package com.sechkarev.justaddhilt.usecases.hilt.annotation

import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.idea.util.application.executeOnPooledThread
import org.jetbrains.kotlin.idea.util.application.runReadAction

@Service
class AddHiltAnnotationToPsiClass(private val project: Project) {

    private val addHiltAnnotationToJavaClass = project.service<AddHiltAnnotationToJavaClass>()
    private val addHiltAnnotationToKotlinClass = project.service<AddHiltAnnotationToKotlinClass>()

    operator fun invoke(psiClass: PsiClass): Boolean {
        var hiltAnnotationAlreadyPresent = false
        executeOnPooledThread {
            hiltAnnotationAlreadyPresent = runReadAction {
                if (project.isDisposed) {
                    false
                } else {
                    psiClass.hasAnnotation("dagger.hilt.android.HiltAndroidApp")
                }
            }
        }.get()
        if (hiltAnnotationAlreadyPresent) {
            return false
        }
        if (psiClass.language is JavaLanguage) {
            addHiltAnnotationToJavaClass(psiClass)
        } else if (psiClass.language is KotlinLanguage) {
            addHiltAnnotationToKotlinClass(psiClass)
        }
        return true
    }
}