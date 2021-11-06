package com.sechkarev.justaddhilt.usecase.generation

import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
import freemarker.template.Version

val freeMarkerConfig by lazy {
    Configuration(Version(Version.intValueFor(2, 3, 31))).apply {
        setClassForTemplateLoading(this.javaClass, "/templates")
        defaultEncoding = Charsets.UTF_8.name()
        templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
    }
}