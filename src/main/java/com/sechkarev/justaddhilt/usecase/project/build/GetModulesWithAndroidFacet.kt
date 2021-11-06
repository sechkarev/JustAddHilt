package com.sechkarev.justaddhilt.usecase.project.build

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.jetbrains.android.facet.AndroidFacet

@Service
class GetModulesWithAndroidFacet(project: Project) {

    private val getBuildModels = project.service<GetBuildModels>()

    operator fun invoke() = getBuildModels()
        .mapNotNull { it.psiElement }
        .mapNotNull { AndroidFacet.getInstance(it) }
        .map { it.module }

}