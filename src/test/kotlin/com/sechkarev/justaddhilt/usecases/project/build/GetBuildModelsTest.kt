package com.sechkarev.justaddhilt.usecases.project.build

import com.android.tools.idea.gradle.project.importing.GradleProjectImporter
import com.android.tools.idea.gradle.project.sync.GradleSyncInvoker
import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.sechkarev.justaddhilt.TestGradleSyncListener
import com.sechkarev.justaddhilt.baseTestPath
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class GetBuildModelsTest : BasePlatformTestCase() {

    @BeforeEach
    override fun setUp() {
        super.setUp()
        UsefulTestCase.assertNotNull(testDataPath)
    }

    override fun getTestDataPath() = "$baseTestPath/GetBuildModelsTest"

    @Test
    fun setupGradleProject_getBuildModels_twoBuildModels() {
        val project = GradleProjectImporter.getInstance().createProject(
            "TestProject",
            File(testDataPath, "App"),
        )
        GradleProjectImporter.configureNewProject(project)
        val testListener = TestGradleSyncListener()
        GradleSyncInvoker.getInstance().requestProjectSync(
            project,
            GradleSyncInvoker.Request.testRequest(),
            testListener,
        )
        testListener.await()

        val getBuildModels = GetBuildModels(project)
        val buildModels = getBuildModels()
        UsefulTestCase.assertEquals(2, buildModels.size)
    }
}