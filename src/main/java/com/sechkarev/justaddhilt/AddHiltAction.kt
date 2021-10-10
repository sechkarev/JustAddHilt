package com.sechkarev.justaddhilt

import com.android.SdkConstants
import com.android.tools.idea.concurrency.addCallback
import com.android.tools.idea.gradle.dsl.api.GradleBuildModel
import com.android.tools.idea.gradle.dsl.api.ProjectBuildModel
import com.android.tools.idea.gradle.dsl.api.dependencies.ArtifactDependencyModel
import com.android.tools.idea.gradle.dsl.api.dependencies.DependenciesModel
import com.android.tools.idea.gradle.dsl.api.ext.GradlePropertyModel
import com.android.tools.idea.gradle.dsl.model.repositories.MavenCentralRepositoryModel
import com.android.tools.idea.gradle.structure.model.meta.ValueAnnotation
import com.android.tools.idea.gradle.structure.model.meta.annotateWith
import com.android.tools.idea.projectsystem.ProjectSystemSyncManager
import com.android.tools.idea.projectsystem.gradle.GradleProjectSystemSyncManager
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.executeCommand
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.XmlRecursiveElementVisitor
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.codeStyle.JavaCodeStyleManager
import com.intellij.psi.util.ClassUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.util.concurrency.SameThreadExecutor
import io.netty.util.concurrent.SingleThreadEventExecutor
import kotlinx.coroutines.guava.await
import org.jetbrains.android.dom.manifest.AndroidManifestXmlFile
import org.jetbrains.android.dom.manifest.getPrimaryManifestXml
import org.jetbrains.android.facet.AndroidFacet
import org.jetbrains.kotlin.idea.structuralsearch.visitor.KotlinRecursiveElementVisitor
import org.jetbrains.kotlin.idea.util.application.runWriteAction
import org.jetbrains.kotlin.idea.util.findAnnotation
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtPsiFactory
import java.util.concurrent.Executor

class AddHiltAction : AnAction() {

    private val logger = logger<AddHiltAction>()

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val projectBuildModel = ProjectBuildModel.get(project)
        val projectGradleBuildModel = projectBuildModel.projectBuildModel // todo: what does this mean if this is null?
        val androidBaseBuildModels = projectBuildModel.allIncludedBuildModels.filter { moduleBuildModel ->
            moduleBuildModel.plugins().any { plugin ->
                plugin.name().getValue(GradlePropertyModel.STRING_TYPE)?.equals("com.android.application") == true
            }
        }
        logger.warn("androidBaseBuildModels: " + androidBaseBuildModels.joinToString { it.moduleRootDirectory.name })
        executeCommand {
            runWriteAction {
                projectGradleBuildModel?.repositories()?.addRepositoryByMethodName(MavenCentralRepositoryModel.MAVEN_CENTRAL_METHOD_NAME)
                androidBaseBuildModels.forEach {
                    if (!isDependencyExist(it.dependencies(), "com.google.dagger:hilt-android:")) {
                        it.dependencies().addArtifact(
                            "implementation",
                            "com.google.dagger:hilt-android:2.39.1"
                        ) // todo: scrap(?) the fresh version
                        it.applyChanges()
                        it.psiElement?.let { psiElement -> CodeStyleManager.getInstance(project).reformat(psiElement) }
                    }
                }
                projectGradleBuildModel?.applyChanges()
                projectGradleBuildModel?.psiElement?.let { CodeStyleManager.getInstance(project).reformat(it) } // todo: reformat only the changes?.. the entire file might be overkill
                val listenableFuture = GradleProjectSystemSyncManager(project).syncProject(ProjectSystemSyncManager.SyncReason.PROJECT_MODIFIED)
                listenableFuture.addListener(
                    { addAnnotationToApplicationClasses(project, androidBaseBuildModels) },
                    { DumbService.getInstance(project).smartInvokeLater(it) },
                )
            }
        }

    }

    private fun addAnnotationToApplicationClasses(project: Project, androidBaseBuildModels: List<GradleBuildModel>) {
        val androidFacets = androidBaseBuildModels.mapNotNull { it.psiElement?.let { psiElement -> AndroidFacet.getInstance(psiElement) } }
        logger.warn("androidFacets: " + androidFacets.joinToString { it.module.name })
        executeCommand {
            runWriteAction {
                androidFacets.forEach { androidFacet ->
                    val primaryManifestXml = androidFacet.getPrimaryManifestXml()
                    val applicationName = primaryManifestXml?.findApplicationName()
                    val packageName = primaryManifestXml?.packageName
                    val fullClassName = packageName + applicationName
                    val applicationPsiClass = ClassUtil.findPsiClass(PsiManager.getInstance(project), fullClassName) ?: run {
                        logger.warn("No class found by name: $fullClassName")
                        return@forEach
                    }
                    val applicationClassHasHiltAnnotation = applicationPsiClass.hasAnnotation("dagger.hilt.android.HiltAndroidApp")
                    val hiltAnnotation = applicationPsiClass.annotations.firstOrNull { it.hasQualifiedName("dagger.hilt.android.HiltAndroidApp") }
                    logger.warn("Class ${applicationPsiClass.qualifiedName} contains hilt annotation = ${hiltAnnotation != null}")
                    if (hiltAnnotation == null) {  // todo: works only in java!
                        val addedAnnotation = applicationPsiClass.modifierList?.addAnnotation("dagger.hilt.android.HiltAndroidApp")
                        addedAnnotation?.let { JavaCodeStyleManager.getInstance(project).shortenClassReferences(it) }
                        CodeStyleManager.getInstance(project).reformat(applicationPsiClass)
                    }
                }
            }
        }
    }

    private fun checkMavenCentral(projectBuildModel: GradleBuildModel?) {
        val mavenCentralPresent = projectBuildModel?.mavenCentralPresent()
        val mavenCentralPresentString = "Maven Central is present in repos = $mavenCentralPresent"
        logger.warn(mavenCentralPresentString)
    }

    private fun GradleBuildModel.mavenCentralPresent() = this
        .repositories()
        .containsMethodCall(MavenCentralRepositoryModel.MAVEN_CENTRAL_METHOD_NAME)

    private fun isDependencyExist(dependenciesModel: DependenciesModel, dependencyName: String): Boolean {
        return dependenciesModel
            .all()
            .any { dependencyModel ->
                dependencyModel is ArtifactDependencyModel
                        && dependencyModel.isEqualName(dependencyName)
            }
    }


    private fun ArtifactDependencyModel.isEqualName(dependencyName: String) =
        getGroupName() == dependencyName


    private fun ArtifactDependencyModel.getGroupName(): String {
        return group().toString() + ":" + name().toString() + ":"
    }

    private fun AndroidManifestXmlFile.findApplicationName(): String? {
        var result: String? = null
        accept(object : XmlRecursiveElementVisitor() {
            override fun visitXmlTag(tag: XmlTag?) {
                super.visitXmlTag(tag)
                if ("application" != tag?.name) return
                tag.getAttributeValue(SdkConstants.ATTR_NAME, SdkConstants.ANDROID_URI)?.let { result = it }
            }
        })
        return result
    }
}