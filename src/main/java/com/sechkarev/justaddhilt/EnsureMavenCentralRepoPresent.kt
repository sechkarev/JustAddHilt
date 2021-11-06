package com.sechkarev.justaddhilt

import com.android.tools.idea.gradle.dsl.api.GradleBuildModel
import com.android.tools.idea.gradle.dsl.api.ProjectBuildModel
import com.android.tools.idea.gradle.dsl.api.repositories.RepositoriesModel
import com.android.tools.idea.gradle.dsl.model.repositories.MavenCentralRepositoryModel
import com.intellij.openapi.command.executeCommand
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.psi.codeStyle.CodeStyleManager
import org.jetbrains.kotlin.idea.util.application.runWriteAction

@Service
class EnsureMavenCentralRepoPresent(private val project: Project) {

    operator fun invoke() {
        executeCommand {
            runWriteAction {
                ProjectBuildModel
                    .get(project)
                    .projectBuildModel?.apply {
                        if (repositories().mavenCentralPresent()) return@apply
                        repositories().addRepositoryByMethodName(MavenCentralRepositoryModel.MAVEN_CENTRAL_METHOD_NAME)
                        applyChanges()
                        psiElement?.let { CodeStyleManager.getInstance(project).reformat(it) }
                    }
                // fixme: when there is no allprojects { ... } block, it is not created!
                // todo: also, the settings file isn't taken into account. if this is present, addition fails (
                //  dependencyResolutionManagement {
                //    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
                //    ...
                //    }
                // )
                // todo: do this ONLY if a dependency/repository was added
            }
        }
    }

    private fun RepositoriesModel.mavenCentralPresent() =
        containsMethodCall(MavenCentralRepositoryModel.MAVEN_CENTRAL_METHOD_NAME)

}