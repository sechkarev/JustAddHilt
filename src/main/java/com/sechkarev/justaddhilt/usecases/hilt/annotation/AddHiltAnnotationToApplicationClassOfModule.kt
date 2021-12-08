package com.sechkarev.justaddhilt.usecases.hilt.annotation

import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.components.service
import com.intellij.openapi.module.Module
import com.intellij.psi.PsiClass
import com.intellij.util.concurrency.AppExecutorUtil
import com.sechkarev.justaddhilt.usecases.project.application.AddApplicationClassToModule
import com.sechkarev.justaddhilt.usecases.project.application.GetModuleApplicationClass
import com.sechkarev.justaddhilt.usecases.project.application.IsApplicationClassGenerationRequiredForModule
import org.jetbrains.kotlin.idea.KotlinLanguage

class AddHiltAnnotationToApplicationClassOfModule(private val module: Module) {

    private val addHiltAnnotationToJavaClass = module.project.service<AddHiltAnnotationToJavaClass>()
    private val addHiltAnnotationToKotlinClass = module.project.service<AddHiltAnnotationToKotlinClass>()
    private val addApplicationClassToModule = AddApplicationClassToModule(module)
    private val getModuleApplicationClass = GetModuleApplicationClass(module)

    operator fun invoke(annotationAddedCallback: (Boolean) -> Unit) {
        val shouldGenerateApplicationClass = IsApplicationClassGenerationRequiredForModule(module)()
        if (shouldGenerateApplicationClass) {
            addApplicationClassToModule { applicationClassAdded ->
                annotationAddedCallback(applicationClassAdded)
            }
        } else {
            addHiltAnnotationToExistingApplicationClass { hiltAnnotationAdded ->
                annotationAddedCallback(hiltAnnotationAdded)
            }
        }
    }

    private fun addHiltAnnotationToExistingApplicationClass(annotationAddedCallback: (Boolean) -> Unit) {
        val applicationPsiClass = getModuleApplicationClass()
        if (applicationPsiClass == null) {
            annotationAddedCallback(false)
            return
        }
        isHiltAnnotationPresent(applicationPsiClass) { annotationAlreadyPresent ->
            if (annotationAlreadyPresent) {
                annotationAddedCallback(false)
            } else {
                if (applicationPsiClass.language is JavaLanguage) {
                    addHiltAnnotationToJavaClass(applicationPsiClass)
                } else if (applicationPsiClass.language is KotlinLanguage) {
                    addHiltAnnotationToKotlinClass(applicationPsiClass)
                }
                annotationAddedCallback(true)
            }
        }
    }

    private fun isHiltAnnotationPresent(
        psiClass: PsiClass,
        annotationPresentCallback: (Boolean) -> Unit,
    ) {
        ReadAction.nonBlocking<Boolean> {
            if (module.project.isDisposed) {
                false
            } else {
                psiClass.hasAnnotation("dagger.hilt.android.HiltAndroidApp")
            }
        }.expireWith(module.project)
            .finishOnUiThread(ModalityState.NON_MODAL) { annotationPresentCallback(it) }
            .submit(AppExecutorUtil.getAppExecutorService())
    }
}