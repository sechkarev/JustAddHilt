package com.sechkarev.justaddhilt.usecase.hilt.annotation

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.sechkarev.justaddhilt.usecase.project.application.AddApplicationClassToModule
import com.sechkarev.justaddhilt.usecase.project.application.GetModuleApplicationClass
import com.sechkarev.justaddhilt.usecase.project.application.IsApplicationClassGenerationRequiredForModule
import com.sechkarev.justaddhilt.usecase.project.build.GetAndroidFacetsOfApplicationModules

@Service
class AddHiltAnnotationToApplicationClasses(project: Project) {

    private val getAndroidFacetsOfApplicationModules = project.service<GetAndroidFacetsOfApplicationModules>()
    private val addHiltAnnotationToPsiClass = project.service<AddHiltAnnotationToPsiClass>()

    operator fun invoke(): Boolean {
        var codeWasAdded = false
        getAndroidFacetsOfApplicationModules()
            .map { it.module }
            .forEach { module ->
                val shouldGenerateApplicationClass = IsApplicationClassGenerationRequiredForModule(module)()
                codeWasAdded = if (shouldGenerateApplicationClass) {
                    val appClassWasGenerated = AddApplicationClassToModule(module)()
                    codeWasAdded || appClassWasGenerated
                } else {
                    val applicationPsiClass = GetModuleApplicationClass(module)() ?: return@forEach
                    val annotationWasAdded = addHiltAnnotationToPsiClass(applicationPsiClass)
                    codeWasAdded || annotationWasAdded
                }
            }
        return codeWasAdded
    }
}