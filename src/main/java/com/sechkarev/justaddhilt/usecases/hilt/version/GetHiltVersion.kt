package com.sechkarev.justaddhilt.usecases.hilt.version

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project

/*
 It might be a good idea to get the fresh version from the internet
 (https://docs.github.com/en/rest/reference/repos#releases),
 but retrieving it will significantly increase plugin's execution time,
 so I'm not sure.
 */
@Service
class GetHiltVersion(private val project: Project) {
    operator fun invoke() = "2.39.1"
}