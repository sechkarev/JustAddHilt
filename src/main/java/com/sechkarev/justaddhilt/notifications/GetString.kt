package com.sechkarev.justaddhilt.notifications

import com.intellij.DynamicBundle
import org.gradle.internal.impldep.org.jetbrains.annotations.Nls
import org.gradle.internal.impldep.org.jetbrains.annotations.NotNull
import org.gradle.internal.impldep.org.jetbrains.annotations.PropertyKey

private object ResourceBundle : DynamicBundle("messages.ActionBundle") {
    @Nls
    fun getString(
        @NotNull @PropertyKey(resourceBundle = "messages.ActionBundle") key: String,
    ): String = getMessage(key)
}

fun getString(key: String) = ResourceBundle.getString(key)