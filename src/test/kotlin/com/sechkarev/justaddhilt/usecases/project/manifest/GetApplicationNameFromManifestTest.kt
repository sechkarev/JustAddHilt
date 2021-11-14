package com.sechkarev.justaddhilt.usecases.project.manifest

import ComputeBaseTestPath
import com.intellij.openapi.command.executeCommand
import com.intellij.openapi.diagnostic.DefaultLogger
import com.intellij.psi.PsiManager
import com.intellij.psi.util.ClassUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.sechkarev.justaddhilt.usecases.hilt.annotation.AddHiltAnnotationToKotlinClass
import com.sechkarev.justaddhilt.usecases.project.build.GetBuildModels
import junit.framework.TestCase
import org.apache.commons.io.FileUtils
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.kotlin.idea.util.application.runWriteAction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class GetApplicationNameFromManifestTest : BasePlatformTestCase() {

    @BeforeEach
    override fun setUp() {
        super.setUp()
        assertThat(testDataPath).isNotNull
    }

    override fun getTestDataPath(): String {
        return ComputeBaseTestPath()()
    }

    @Test
    fun aTest() {
        val list2 = FileUtils.listFiles(File(testDataPath), arrayOf("kt", "java", "xml", "gradle"), true).map { it.path }
        list2.joinToString().also { DefaultLogger.getInstance(this.javaClass).warn("listed files: $it") }
        myFixture.configureByFiles(*list2.toTypedArray())
        val getBuildModels = GetBuildModels(project)
        val buildModels = getBuildModels()
        TestCase.assertEquals(1, buildModels.size)
//        TestCase.assertTrue(module.isAndroidModule)
//        val getApplicationNameFromManifest = GetApplicationNameFromManifest(module)
//        TestCase.assertEquals(".app.App2Application", getApplicationNameFromManifest())
    }

    @Test
    fun bTest() {
        myFixture.configureByFile("App.kt")
        val psiClass = ClassUtil.findPsiClass(PsiManager.getInstance(project), "GeneratedApplication")
        if (psiClass == null) {
            fail()
            return
        }
        executeCommand {
            runWriteAction {
                AddHiltAnnotationToKotlinClass(project)(psiClass)
            }
        }
    }
}