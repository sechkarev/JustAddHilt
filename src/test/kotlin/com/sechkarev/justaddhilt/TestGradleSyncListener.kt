package com.sechkarev.justaddhilt

import com.android.tools.idea.gradle.project.sync.GradleSyncListener
import com.android.utils.TraceUtils
import com.intellij.openapi.project.Project
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class TestGradleSyncListener : GradleSyncListener {
    private val myLatch: CountDownLatch = CountDownLatch(1)
    var isSyncSkipped = false
    var success = false
    var failureMessage: String? = null

    override fun syncSkipped(project: Project) {
        syncSucceeded(project)
        isSyncSkipped = true
    }

    override fun syncSucceeded(project: Project) {
        success = true
        myLatch.countDown()
    }

    override fun syncFailed(project: Project, errorMessage: String) {
        success = false
        failureMessage =
            if (!errorMessage.isEmpty()) errorMessage else "No errorMessage at:\n${TraceUtils.getCurrentStack()}"
        myLatch.countDown()
    }

    fun await() {
        myLatch.await(5, TimeUnit.MINUTES)
    }

    val isSyncFinished: Boolean
        get() = success || failureMessage != null

}