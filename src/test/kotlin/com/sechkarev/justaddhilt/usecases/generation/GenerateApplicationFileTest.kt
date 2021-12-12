package com.sechkarev.justaddhilt.usecases.generation

import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.sechkarev.justaddhilt.baseTestPath
import com.sechkarev.justaddhilt.generation.ApplicationFileLanguage
import com.sechkarev.justaddhilt.generation.ApplicationFileProperties
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GenerateApplicationFileTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String = baseTestPath

    @BeforeEach
    override fun setUp() {
        super.setUp()
        UsefulTestCase.assertNotNull(testDataPath)
    }

    override fun isWriteActionRequired() = true

    @Test
    fun generateApplicationFile_kotlin_fileTextMatchesFreeMarkerTemplate() {
        runTestRunnable {
            val useCase = GenerateApplicationFile(project)
            val applicationPsiFile = useCase(
                packageName = packageName,
                applicationFileProperties = ApplicationFileProperties(
                    name = className,
                    ApplicationFileLanguage.KOTLIN,
                )
            )
            UsefulTestCase.assertEquals(
                expectedAppFileText,
                applicationPsiFile.text,
            )
        }
    }

    private companion object {
        const val packageName = "com.sechkarev.justaddhilt"
        const val className = "TestApplication"
        val expectedAppFileText = """
            package $packageName

            import android.app.Application
            import dagger.hilt.android.HiltAndroidApp
            
            @HiltAndroidApp
            class $className : Application() {
            }
            """.trimIndent().trimStart().trimEnd()
    }
}