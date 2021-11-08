package com.sechkarev.justaddhilt.usecase.project.manifest

import ComputeBasePath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetApplicationNameFromManifestTest : BasePlatformTestCase() {

    @BeforeEach
    override fun setUp() {
        super.setUp()
        assertThat(testDataPath).isNotNull
    }

    override fun getTestDataPath(): String {
        return ComputeBasePath()()
    }

    @Test
    fun aTest() {
        myFixture.configureByFile("testFile.gradle")
        myFixture.project
    }
}