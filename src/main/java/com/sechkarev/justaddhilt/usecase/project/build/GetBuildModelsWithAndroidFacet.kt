package com.sechkarev.justaddhilt.usecase.project.build

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.jetbrains.android.facet.AndroidFacet

@Service
class GetBuildModelsWithAndroidFacet(private val project: Project) {
    operator fun invoke() = project
        .service<GetBuildModels>()()
        .filter { buildModel -> buildModel.psiElement?.let { AndroidFacet.getInstance(it) } != null }
}