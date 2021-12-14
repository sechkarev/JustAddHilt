package com.sechkarev.justaddhilt.usecases.project.manifest

import com.android.tools.idea.gradle.project.importing.GradleProjectImporter
import com.android.tools.idea.gradle.project.sync.GradleSyncInvoker
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemJdkUtil
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.sechkarev.justaddhilt.TestGradleSyncListener
import com.sechkarev.justaddhilt.baseTestPath
import org.jetbrains.kotlin.idea.util.projectStructure.allModules
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class GetApplicationNameFromManifestTest : BasePlatformTestCase() {

    @BeforeEach
    override fun setUp() {
        super.setUp()
        Assertions.assertNotNull(testDataPath)
    }

    override fun getTestDataPath() = "$baseTestPath/GetApplicationNameFromManifestTest"

    override fun isWriteActionRequired() = true

    @Test
    fun getAppNameFromManifest() {
        runTestRunnable {
            val project = GradleProjectImporter.getInstance().createProject(
                "TestProject",
                File(testDataPath, "App"),
            )
            val externalSystemJdk = ExternalSystemJdkUtil.getAvailableJdk(project).second
            // todo: I feel like I should retrieve  a valid JDK from somewhere else
            ProjectJdkTable.getInstance().addJdk(externalSystemJdk)
            ProjectRootManager.getInstance(project).projectSdk = externalSystemJdk
            GradleProjectImporter.configureNewProject(project)
            val testListener = TestGradleSyncListener()
            GradleSyncInvoker.getInstance().requestProjectSync(
                project,
                GradleSyncInvoker.Request.testRequest(),
                testListener,
            )
            testListener.await()

            val modules = project.allModules()

            val appName = GetApplicationNameFromManifest(modules[1])()
            Assertions.assertEquals(".CustomApplication", appName)
        }
    }
}