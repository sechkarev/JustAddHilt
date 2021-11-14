package com.sechkarev.justaddhilt.usecase.generation

import ComputeBaseTestPath
import com.intellij.configurationStore.runAsWriteActionIfNeeded
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.executeCommand
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.sechkarev.justaddhilt.generation.ApplicationFileLanguage
import com.sechkarev.justaddhilt.generation.ApplicationFileProperties
import org.assertj.core.api.Assertions
import org.jetbrains.kotlin.idea.util.application.runWriteAction
import org.jetbrains.kotlin.idea.util.application.runWriteActionInEdt
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test

class GenerateApplicationFileTest : BasePlatformTestCase() {

    override fun getTestDataPath() = ComputeBaseTestPath()()

    @BeforeEach
    override fun setUp() {
        super.setUp()
        Assertions.assertThat(testDataPath).isNotNull
    }

    @Test
    fun ttt() {
        val project = myFixture.project
        val useCase = GenerateApplicationFile(project)
        runWriteAction {
//                val file = useCase("aaa", ApplicationFileProperties("aaa", ApplicationFileLanguage.KOTLIN))
        }
    }
}