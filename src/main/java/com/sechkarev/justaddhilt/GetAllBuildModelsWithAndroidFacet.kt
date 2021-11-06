package com.sechkarev.justaddhilt

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.jetbrains.android.facet.AndroidFacet

@Service
class GetAllBuildModelsWithAndroidFacet(private val project: Project) {
    operator fun invoke() = project
        .service<GetAllBuildModels>()()
        .filter { buildModel -> buildModel.psiElement?.let { AndroidFacet.getInstance(it) } != null }
}