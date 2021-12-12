package com.sechkarev.justaddhilt.usecases.hilt.annotation

import com.intellij.psi.PsiManager
import com.intellij.psi.util.ClassUtil
import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.sechkarev.justaddhilt.baseTestPath
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AddHiltAnnotationToJavaClassTest : BasePlatformTestCase() {

    @BeforeEach
    override fun setUp() {
        super.setUp()
        UsefulTestCase.assertNotNull(testDataPath)
    }

    override fun getTestDataPath() = "$baseTestPath/AddHiltAnnotationToJavaClassTest"

    override fun isWriteActionRequired() = true

    @Test
    fun addHiltAnnotationToKotlinClass_topLevelClass() {
        myFixture.copyFileToProject(fileName)
        myFixture.configureFromTempProjectFile(fileName)
        runTestRunnable {
            val psiClass = ClassUtil.findPsiClass(PsiManager.getInstance(project), className)
            AddHiltAnnotationToJavaClass(project)(psiClass!!)
            UsefulTestCase.assertTrue(myFixture.file.text.contains("dagger.hilt.android.HiltAndroidApp"))
        }
    }

    private companion object {
        const val fileName = "TestApplication.java"
        const val className = "TestApplication"
    }
}