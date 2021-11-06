package com.sechkarev.justaddhilt.action

import com.android.SdkConstants
import com.android.tools.idea.projectsystem.ProjectSystemSyncManager
import com.android.tools.idea.projectsystem.gradle.GradleProjectSystemSyncManager
import com.android.tools.idea.util.androidFacet
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.executeCommand
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.XmlRecursiveElementVisitor
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.codeStyle.JavaCodeStyleManager
import com.intellij.psi.util.ClassUtil
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.refactoring.suggested.startOffset
import com.sechkarev.justaddhilt.usecase.generation.AddApplicationFileToModule
import com.sechkarev.justaddhilt.usecase.generation.ApplicationClassExistsInModule
import com.sechkarev.justaddhilt.usecase.hilt.dependency.AddHiltDependenciesToAndroidModules
import com.sechkarev.justaddhilt.usecase.project.KotlinEnabledInProject
import com.sechkarev.justaddhilt.usecase.project.build.GetApplicationBuildModels
import com.sechkarev.justaddhilt.usecase.project.build.GetBuildModels
import com.sechkarev.justaddhilt.usecase.project.build.GetModulesWithAndroidFacet
import com.sechkarev.justaddhilt.usecase.project.repository.EnsureMavenCentralRepositoryPresent
import org.jetbrains.android.dom.manifest.AndroidManifestXmlFile
import org.jetbrains.android.dom.manifest.getPrimaryManifestXml
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.idea.util.application.runWriteAction
import org.jetbrains.kotlin.psi.KtClass

class AddHiltAction : AnAction() {

    private val logger = logger<AddHiltAction>()

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return // todo: show error

        val buildModels = project.service<GetBuildModels>()()
        project.service<EnsureMavenCentralRepositoryPresent>()()
        logger.warn("build models: ${buildModels.joinToString { it.moduleRootDirectory.name }}")
        // todo: show error if this list is empty
        executeLogic(e)
    }

    private fun executeLogic(e: AnActionEvent) {
        val project = e.project ?: return

        val kotlinEnabledInProject = project.service<KotlinEnabledInProject>()()
        logger.warn("Kotlin plugin enabled in project = $kotlinEnabledInProject")
        val androidBaseBuildModels = project.service<GetApplicationBuildModels>()()
        logger.warn("androidBaseBuildModels: " + androidBaseBuildModels.joinToString { it.moduleRootDirectory.name })
        project.service<AddHiltDependenciesToAndroidModules>()()
        val listenableFuture = GradleProjectSystemSyncManager(project).syncProject(ProjectSystemSyncManager.SyncReason.PROJECT_MODIFIED)
        listenableFuture.addListener(
            { addAnnotationToApplicationClasses(project) },
            { DumbService.getInstance(project).smartInvokeLater(it) },
        )
    }

    private fun addAnnotationToApplicationClasses(project: Project) {
        val modulesWithAndroidFacet = project.service<GetModulesWithAndroidFacet>()
        executeCommand {
            runWriteAction {
                modulesWithAndroidFacet().forEach { module ->
                    val applicationFileExistsInModule = module.getService(ApplicationClassExistsInModule::class.java)
                    if (!applicationFileExistsInModule()) {
                        val newFileName = "GeneratedApplication" // todo: what if it already exists?
                        val addApplicationFileToModule = module.getService(AddApplicationFileToModule::class.java)
                        addApplicationFileToModule(newFileName)
                    } else {
                        val primaryManifestXml = module.androidFacet?.getPrimaryManifestXml()
                        val packageName = primaryManifestXml?.packageName
                        val applicationName = primaryManifestXml?.findApplicationName()
                        val fullClassName = packageName + applicationName
                        val applicationPsiClass = ClassUtil.findPsiClass(PsiManager.getInstance(project), fullClassName) ?: run {
                            logger.warn("No class found by name: $fullClassName")
                            return@forEach
                        }
                        val applicationClassHasHiltAnnotation = applicationPsiClass.hasAnnotation("dagger.hilt.android.HiltAndroidApp")
                        val hiltAnnotation = applicationPsiClass.annotations.firstOrNull { it.hasQualifiedName("dagger.hilt.android.HiltAndroidApp") }
                        logger.warn("Class ${applicationPsiClass.qualifiedName} written in ${applicationPsiClass.language} contains hilt annotation = ${hiltAnnotation != null}")
                        // fixme: annotation is added twice if gradle sync fails.
                        if (hiltAnnotation == null) {
                            if (applicationPsiClass.language is JavaLanguage) {
                                val addedAnnotation =
                                    applicationPsiClass.modifierList?.addAnnotation("dagger.hilt.android.HiltAndroidApp")
                                addedAnnotation?.let {
                                    JavaCodeStyleManager.getInstance(project).shortenClassReferences(it)
                                }
                                CodeStyleManager.getInstance(project).reformat(applicationPsiClass)
                                logger.warn("Adding Hilt annotation to Java class ${applicationPsiClass.qualifiedName}")
                            } else if (applicationPsiClass.language is KotlinLanguage) {
                                logger.warn("Application PSI class is ${applicationPsiClass::class.qualifiedName}, can be cast to KtClass = ${applicationPsiClass is KtClass}")
                                // applicationPsiClass is an Ultra Light CLass whatever this is fuck you, and modifierList?.addAnnotation throws UnsupportedOperationException or smth
                                logger.warn("Adding Hilt annotation to Kotlin class ${applicationPsiClass.qualifiedName} inside file ${applicationPsiClass.containingFile.name}")
                                logger.warn("File with app class contains the following elements: ${PsiTreeUtil.getChildrenOfAnyType(applicationPsiClass.containingFile, PsiElement::class.java).filterNotNull().joinToString { it.javaClass.canonicalName ?: it.javaClass.name ?: "null" }}")
                                val document = PsiDocumentManager.getInstance(project).getDocument(applicationPsiClass.containingFile)
                                document?.insertString(applicationPsiClass.startOffset, "@dagger.hilt.android.HiltAndroidApp\n")
                                // todo: shorten class references and reformat
                            }
                        }
                    }
                }
            }
        }
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