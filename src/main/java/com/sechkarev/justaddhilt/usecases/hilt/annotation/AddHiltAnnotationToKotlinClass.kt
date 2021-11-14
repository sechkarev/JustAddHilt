package com.sechkarev.justaddhilt.usecases.hilt.annotation

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiDocumentManager
import com.intellij.refactoring.suggested.startOffset

/*
 Unfortunately, we can't add an annotation to a Kotlin class via PSI tree as we did to a Java class
 because what we have here as a PsiClass is an instance of KtUltraLightClass, and calling addAnnotation
 throws UnsupportedOperationException
 */
@Service
class AddHiltAnnotationToKotlinClass(private val project: Project) {

    operator fun invoke(psiClass: PsiClass) {
        val document = PsiDocumentManager.getInstance(project).getDocument(psiClass.containingFile)
        document?.insertString(psiClass.startOffset, "@dagger.hilt.android.HiltAndroidApp\n")
        // todo: shorten class references and reformat
    }
}