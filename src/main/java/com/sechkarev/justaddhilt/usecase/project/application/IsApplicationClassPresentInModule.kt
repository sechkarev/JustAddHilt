package com.sechkarev.justaddhilt.usecase.project.application

import com.android.tools.idea.util.androidFacet
import com.intellij.openapi.components.Service
import com.intellij.openapi.module.Module
import com.sechkarev.justaddhilt.usecase.project.manifest.GetApplicationNameFromManifest
import org.jetbrains.android.dom.manifest.getPrimaryManifestXml

@Service
class IsApplicationClassPresentInModule(private val module: Module) {

    private val getCurrentApplicationName = GetApplicationNameFromManifest(module)

    operator fun invoke(): Boolean {
        val androidFacet = module.androidFacet ?: return false
        val primaryManifestXml = androidFacet.getPrimaryManifestXml()
        val packageName = primaryManifestXml?.packageName
        val currentApplicationName = getCurrentApplicationName()
        return currentApplicationName == null && packageName != null
    }
}