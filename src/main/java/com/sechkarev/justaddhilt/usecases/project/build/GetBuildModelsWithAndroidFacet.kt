package com.sechkarev.justaddhilt.usecases.project.build

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.jetbrains.android.facet.AndroidFacet

@Service
class GetBuildModelsWithAndroidFacet(project: Project) {

    private val getBuildModels = project.service<GetBuildModels>()

    operator fun invoke() = getBuildModels()
        .filter { buildModel -> buildModel.psiElement?.let { AndroidFacet.getInstance(it) } != null }}