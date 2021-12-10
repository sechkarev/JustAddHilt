package com.sechkarev.justaddhilt.usecases.hilt.annotation

import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.command.executeCommand
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiImportList
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.asJava.KotlinAsJavaSupport
import org.jetbrains.kotlin.idea.util.addAnnotation
import org.jetbrains.kotlin.name.FqName

/*
 Unfortunately, we have to search for the class to add the annotation to yet another time
 because the argument of invoke(...) is an instance of KtUltraLightClass, and calling addAnnotation on it
 throws UnsupportedOperationException
 */
@Service
class AddHiltAnnotationToKotlinClass(private val project: Project) {

    operator fun invoke(psiClass: PsiClass) {
        val psiClassQualifiedName = psiClass.qualifiedName ?: return
        executeCommand(project, "Add Hilt Annotation To Class ${psiClass.name}") {
            runWriteAction {
                val kotlinClass = KotlinAsJavaSupport
                    .getInstance(project)
                    .findClassOrObjectDeclarations(
                        FqName(psiClassQualifiedName),
                        GlobalSearchScope.fileScope(psiClass.containingFile)
                    )
                    .first()
                kotlinClass.addAnnotation(FqName("dagger.hilt.android.HiltAndroidApp"))
                val codeStyleManager = CodeStyleManager.getInstance(project)
                codeStyleManager.reformat(kotlinClass)
                PsiTreeUtil.findChildOfType(kotlinClass.parent, PsiImportList::class.java)?.let {
                    codeStyleManager.reformat(it)
                }
            }
        }
    }
}