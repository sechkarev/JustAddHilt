package com.sechkarev.justaddhilt.usecases.hilt.annotation

import com.intellij.openapi.command.executeCommand
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiImportList
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.codeStyle.JavaCodeStyleManager
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.idea.util.application.runWriteAction

@Service
class AddHiltAnnotationToJavaClass(private val project: Project) {

    operator fun invoke(psiClass: PsiClass) {
        executeCommand(project, "Add Hilt Annotation To Class ${psiClass.name}") {
            runWriteAction {
                val addedAnnotation = psiClass
                    .modifierList
                    ?.addAnnotation("dagger.hilt.android.HiltAndroidApp")
                addedAnnotation?.let { JavaCodeStyleManager.getInstance(project).shortenClassReferences(it) }
                addedAnnotation?.parent?.let { CodeStyleManager.getInstance(project).reformat(it) }
                PsiTreeUtil.findChildOfType(addedAnnotation?.containingFile, PsiImportList::class.java)?.let {
                    CodeStyleManager.getInstance(project).reformat(it)
                }
            }
        }
    }
}