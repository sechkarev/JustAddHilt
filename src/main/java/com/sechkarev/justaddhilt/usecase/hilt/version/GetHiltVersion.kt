package com.sechkarev.justaddhilt.usecase.hilt.version

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project

/*
 todo: might be a good idea to get the fresh version from the internet
 (https://docs.github.com/en/rest/reference/repos#releases),
 but retrieving it will slow the addition down, so I'm not sure.
 */
@Service
class GetHiltVersion(private val project: Project) {
    operator fun invoke() = "2.39.1"
}