package com.sechkarev.justaddhilt.action

import ComputeBasePath
import com.android.tools.idea.gradle.dsl.api.GradleModelProvider
import com.android.tools.idea.gradle.dsl.model.ProjectBuildModelImpl
import com.intellij.facet.FacetManager
import com.intellij.facet.impl.FacetUtil
import com.intellij.javaee.ExternalResourceManagerExImpl
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.command.executeCommand
import com.intellij.openapi.diagnostic.DefaultLogger
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.VfsTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.apache.commons.io.FileUtils
import org.assertj.core.api.Assertions
import org.jetbrains.android.facet.AndroidFacet
import org.jetbrains.android.facet.AndroidFacetConfiguration
import org.jetbrains.android.facet.AndroidFacetType
import org.jetbrains.android.facet.AndroidGradleFacetEditorForIdea
import org.jetbrains.kotlin.idea.util.application.runWriteActionInEdt

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.io.path.Path

class AddHiltActionTest : BasePlatformTestCase() {

    override fun getTestDataPath() = ComputeBasePath()()

    @BeforeEach
    override fun setUp() {
        super.setUp()
        Assertions.assertThat(testDataPath).isNotNull
    }

    @Test
    fun testaTest() {
//        files.forEach {
//            myFixture.configureFromExistingVirtualFile(myFixture.copyFileToProject(it))
//        }
//        val directory = myFixture.copyDirectoryToProject("testCase1", "")
//

//        val androidFacetType = AndroidFacetType()
//        executeCommand {
//            runWriteAction {
//                FacetManager.getInstance(module).addFacet(androidFacetType, androidFacetType.defaultFacetName, null)
//            }
//        }

        val list2 = FileUtils.listFiles(File(testDataPath, "testCase1"), arrayOf("kt", "java", "xml", "gradle"), true).map { it.path }
        val psiFiles = myFixture.configureByFiles(*list2.toTypedArray())

//        PlatformTestUtil.loadAndOpenProject(Path(testDataPath, "testCase1")) { }

//        val buildModel = GradleModelProvider.getInstance().getProjectModel(project)
//        val parsedBuildModel = GradleModelProvider.getInstance().parseBuildFile(psiFiles[9].virtualFile, project)

        val result = myFixture.testAction(AddHiltAction()) // no android modules present, what a pity

        myFixture.checkResult(files[0], "aaa nu kak tak", true)
    }

    companion object {
        val files = arrayOf(
            "testCase1/app5/build.gradle",
            "testCase1/app5/src/main/AndroidManifest.xml",
            "testCase1/app5/src/main/java/com/example/app5/CustomApplication.kt",
        )
    }
}