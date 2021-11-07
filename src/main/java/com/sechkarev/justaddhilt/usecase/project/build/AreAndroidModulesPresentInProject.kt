package com.sechkarev.justaddhilt.usecase.project.build

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

@Service
class AreAndroidModulesPresentInProject(project: Project) {

    private val getBuildModelsWithAndroidFacet = project.service<GetBuildModelsWithAndroidFacet>()

    operator fun invoke() = getBuildModelsWithAndroidFacet().isNotEmpty()
}