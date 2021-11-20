package com.sechkarev.justaddhilt.usecases.hilt.annotation

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.refactoring.suggested.startOffset
import com.intellij.util.FileContentUtil
import org.jetbrains.kotlin.idea.core.ShortenReferences
import org.jetbrains.kotlin.psi.KtFile

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
        FileContentUtil.reparseFiles(
            project,
            listOf(psiClass.containingFile.virtualFile),
            false,
        )
        // fixme: the KtFile does not contain the new annotation
        document
            ?.let { PsiDocumentManager.getInstance(project).getPsiFile(it) as KtFile }
            ?.let { ShortenReferences.DEFAULT.process(it) }
        psiClass.containingFile?.let { CodeStyleManager.getInstance(project).reformat(it) }
    }
}