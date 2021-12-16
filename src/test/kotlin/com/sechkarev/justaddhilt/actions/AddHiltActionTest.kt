package com.sechkarev.justaddhilt.actions

import com.android.tools.idea.gradle.project.importing.GradleProjectImporter
import com.android.tools.idea.gradle.project.sync.GradleSyncInvoker
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemJdkUtil
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.testFramework.TestActionEvent
import com.intellij.testFramework.TestDataProvider
import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.sechkarev.justaddhilt.TestGradleSyncListener
import com.sechkarev.justaddhilt.baseTestPath
import com.sechkarev.justaddhilt.usecases.project.manifest.GetApplicationNameFromManifest
import org.jetbrains.kotlin.idea.util.projectStructure.allModules
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class AddHiltActionTest : BasePlatformTestCase() {

    @BeforeEach
    override fun setUp() {
        super.setUp()
        UsefulTestCase.assertNotNull(testDataPath)
    }

    override fun getTestDataPath() = "$baseTestPath/AddHiltActionTest"

    override fun isWriteActionRequired() = true

    @Test
    fun addHiltAction() {
        runTestRunnable {
            val project = GradleProjectImporter.getInstance().createProject(
                "TestProject",
                File(testDataPath, "App"),
            )
            val externalSystemJdk = ExternalSystemJdkUtil.getAvailableJdk(project).second
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

            val action = AddHiltAction()
            val event = TestActionEvent(TestDataProvider(project), action)
            ActionUtil.performActionDumbAwareWithCallbacks(action, event)

            // we need to wait until the end?

            val modules = project.allModules()
            UsefulTestCase.assertEquals(".CustomApplication", GetApplicationNameFromManifest(modules[1])())
        }
    }
}