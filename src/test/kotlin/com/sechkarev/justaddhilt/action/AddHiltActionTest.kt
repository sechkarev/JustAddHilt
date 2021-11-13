package com.sechkarev.justaddhilt.action

import ComputeBasePath
import com.android.tools.idea.gradle.dsl.api.GradleModelProvider
import com.android.tools.idea.gradle.dsl.model.ProjectBuildModelImpl
import com.android.tools.idea.gradle.run.create
import com.intellij.facet.FacetManager
import com.intellij.facet.impl.FacetUtil
import com.intellij.javaee.ExternalResourceManagerExImpl
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.command.executeCommand
import com.intellij.openapi.diagnostic.DefaultLogger
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ContentEntry
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.VfsTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.apache.commons.io.FileUtils
import org.assertj.core.api.Assertions
import org.jetbrains.android.facet.AndroidFacet
import org.jetbrains.android.facet.AndroidFacetConfiguration
import org.jetbrains.android.facet.AndroidFacetType
import org.jetbrains.android.facet.AndroidGradleFacetEditorForIdea
import org.jetbrains.kotlin.idea.facet.getOrCreateFacet
import org.jetbrains.kotlin.idea.util.application.runWriteActionInEdt

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path

class AddHiltActionTest : BasePlatformTestCase() {

    override fun getTestDataPath() = ComputeBasePath()()

    @BeforeEach
    override fun setUp() {
        super.setUp()
        Assertions.assertThat(testDataPath).isNotNull
    }

    override fun getProjectDescriptor() = TestProjectDescriptor()

    @Test
    fun testaTest() {
//        files.forEach {
//            myFixture.configureFromExistingVirtualFile(myFixture.copyFileToProject(it))
//        }
//        val directory = myFixture.copyDirectoryToProject(testDataFolder, "")
//        myFixture.configureFromExistingVirtualFile(directory)


        val list2 = FileUtils.listFiles(
            File(testDataPath, testDataFolder),
            arrayOf("kt", "java", "xml", "gradle", "gitignore", "pro", "properties"),
            true
        ).map { it.path }
        list2.forEach { myFixture.configureByFile(it) }
//        val psiFiles = myFixture.configureByFiles(*list2.toTypedArray())

//        PlatformTestUtil.loadAndOpenProject(Path(testDataPath, testDataFolder)) { }

//        val buildModel = GradleModelProvider.getInstance().getProjectModel(project)
//        val parsedBuildModel = GradleModelProvider.getInstance().parseBuildFile(psiFiles[9].virtualFile, project)

        val result = myFixture.testAction(AddHiltAction()) // no android modules present, what a pity

        myFixture.checkResult("aaa nu kak tak", true)
    }

    companion object {
        const val testDataFolder = "EmptyTestApp"

        val files = arrayOf(
            "$testDataFolder/app/build.gradle",
            "$testDataFolder/app/src/main/AndroidManifest.xml",
            "$testDataFolder/app/src/main/java/com/example/app5/CustomApplication.kt",
        )
    }

    class TestProjectDescriptor : LightProjectDescriptor() {
        override fun configureModule(module: Module, model: ModifiableRootModel, contentEntry: ContentEntry) {

        }
    }
}