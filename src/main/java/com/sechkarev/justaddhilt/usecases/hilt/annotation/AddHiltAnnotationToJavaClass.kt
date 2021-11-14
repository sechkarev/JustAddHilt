package com.sechkarev.justaddhilt.usecases.hilt.annotation

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.codeStyle.JavaCodeStyleManager

@Service
class AddHiltAnnotationToJavaClass(private val project: Project) {

    operator fun invoke(psiClass: PsiClass) {
        val addedAnnotation = psiClass
            .modifierList
            ?.addAnnotation("dagger.hilt.android.HiltAndroidApp")
        addedAnnotation?.let { JavaCodeStyleManager.getInstance(project).shortenClassReferences(it) }
        CodeStyleManager.getInstance(project).reformat(psiClass)
    }
}