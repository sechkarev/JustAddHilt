package com.sechkarev.justaddhilt.ext

import com.android.tools.idea.gradle.dsl.api.dependencies.ArtifactDependencyModel

fun ArtifactDependencyModel.getGroupName(): String {
    return group().toString() + ":" + name().toString()
}