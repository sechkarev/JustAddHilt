package com.sechkarev.justaddhilt.actions

import ComputeBaseTestPath
import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.ContentEntry
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.apache.commons.io.FileUtils
import org.assertj.core.api.Assertions

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class AddHiltActionTest : BasePlatformTestCase() {

    override fun getTestDataPath() = ComputeBaseTestPath()()

    @BeforeEach
    override fun setUp() {
        super.setUp()
        Assertions.assertThat(testDataPath).isNotNull
    }

    override fun getProjectDescriptor() = TestProjectDescriptor()

    @Test
    fun testaTest() {
        val list2 = FileUtils.listFiles(
            File(testDataPath, testDataFolder),
            arrayOf("kt", "java", "xml", "gradle", "gitignore", "pro", "properties"),
            true
        ).map { it.path }
        val virtualFiles = mutableListOf<Any>()
        list2.forEach {
            myFixture.copyFileToProject(it).also {
                println(it.path)
            }
            myFixture.configureByFile(it)
        }
//        val psiFiles = myFixture.configureByFiles(*list2.toTypedArray())

//        PlatformTestUtil.loadAndOpenProject(Path(testDataPath, testDataFolder)) { }

//        val buildModel = GradleModelProvider.getInstance().getProjectModel(project)
//        val parsedBuildModel = GradleModelProvider.getInstance().parseBuildFile(psiFiles[9].virtualFile, project)

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