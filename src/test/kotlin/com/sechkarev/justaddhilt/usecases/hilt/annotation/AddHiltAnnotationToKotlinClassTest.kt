package com.sechkarev.justaddhilt.usecases.hilt.annotation

import com.intellij.psi.PsiManager
import com.intellij.psi.util.ClassUtil
import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.sechkarev.justaddhilt.baseTestPath
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AddHiltAnnotationToKotlinClassTest : BasePlatformTestCase() {

    @BeforeEach
    override fun setUp() {
        super.setUp()
        UsefulTestCase.assertNotNull(testDataPath)
    }

    override fun getTestDataPath() = "$baseTestPath/AddHiltAnnotationToKotlinClassTest"

    override fun isWriteActionRequired() = true

    @Test
    fun addHiltAnnotationToKotlinClass_topLevelClass() {
        myFixture.copyFileToProject(fileName)
        myFixture.configureFromTempProjectFile(fileName)
        runTestRunnable {
            val psiClass = ClassUtil.findPsiClass(PsiManager.getInstance(project), className)
            AddHiltAnnotationToKotlinClass(project)(psiClass!!)
            UsefulTestCase.assertTrue(myFixture.file.text.contains("dagger.hilt.android.HiltAndroidApp"))
            // fixme: the annotation doesn't get shortened?
        }
    }

    private companion object {
        const val fileName = "TestApplication.kt"
        const val className = "TestApplication"
        const val expectedText =
            """
            import android.app.Application
            import dagger.hilt.android.HiltAndroidApp
            
            @HiltAndroidApp
            class $className : Application() {
            }
            """
    }
}