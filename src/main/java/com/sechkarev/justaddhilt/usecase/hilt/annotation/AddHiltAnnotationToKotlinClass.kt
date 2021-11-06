package com.sechkarev.justaddhilt.usecase.hilt.annotation

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiDocumentManager
import com.intellij.refactoring.suggested.startOffset

@Service
class AddHiltAnnotationToKotlinClass(private val project: Project) {

    operator fun invoke(psiClass: PsiClass) {
        // applicationPsiClass is an Ultra Light CLass whatever this is fuck you, and modifierList?.addAnnotation throws UnsupportedOperationException or smth
        val document = PsiDocumentManager.getInstance(project).getDocument(psiClass.containingFile)
        document?.insertString(psiClass.startOffset, "@dagger.hilt.android.HiltAndroidApp\n")
        // todo: shorten class references and reformat
    }
}