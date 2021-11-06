package com.sechkarev.justaddhilt.usecase.hilt.version

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project

@Service
// todo: scrap(?) the fresh version (github releases?)
// https://docs.github.com/en/rest/reference/repos#releases
class GetHiltVersion(private val project: Project) {
    operator fun invoke() = "2.39.1"
}