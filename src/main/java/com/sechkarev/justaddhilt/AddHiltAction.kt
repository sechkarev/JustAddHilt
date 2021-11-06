package com.sechkarev.justaddhilt

import com.android.SdkConstants
import com.android.tools.idea.gradle.dsl.api.GradleBuildModel
import com.android.tools.idea.gradle.dsl.api.PluginModel
import com.android.tools.idea.gradle.dsl.api.ProjectBuildModel
import com.android.tools.idea.gradle.dsl.api.dependencies.ArtifactDependencyModel
import com.android.tools.idea.gradle.dsl.api.dependencies.DependenciesModel
import com.android.tools.idea.gradle.dsl.api.ext.GradlePropertyModel
import com.android.tools.idea.gradle.dsl.model.repositories.MavenCentralRepositoryModel
import com.android.tools.idea.projectsystem.ProjectSystemSyncManager
import com.android.tools.idea.projectsystem.gradle.GradleProjectSystemSyncManager
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.executeCommand
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessModuleDir
import com.intellij.psi.*
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.codeStyle.JavaCodeStyleManager
import com.intellij.psi.util.ClassUtil
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.refactoring.suggested.startOffset
import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
import freemarker.template.Version
import org.jetbrains.android.dom.manifest.AndroidManifestXmlFile
import org.jetbrains.android.dom.manifest.getPrimaryManifestXml
import org.jetbrains.android.facet.AndroidFacet
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.idea.util.application.runWriteAction
import org.jetbrains.kotlin.psi.KtClass
import java.io.StringWriter

class AddHiltAction : AnAction() {

    private val logger = logger<AddHiltAction>()

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return // todo: show error

        val buildModelsWithAndroidFacet = project.service<GetAllBuildModelsWithAndroidFacet>()()
        val buildModels = project.service<GetAllBuildModels>()()
        project.service<EnsureMavenCentralRepoPresent>()()
        logger.warn("build models: ${buildModels.joinToString { it.moduleRootDirectory.name }}")
        logger.warn("build models With Android Facet: ${buildModelsWithAndroidFacet.joinToString { it.moduleRootDirectory.name }}")
        // todo: show error if this list is empty
        executeLogic(e)
    }

    private fun executeLogic(e: AnActionEvent) {
        val project = e.project ?: return

        val kotlinEnabledInProject = project.service<KotlinEnabledInProject>()()
        logger.warn("Kotlin plugin enabled in project = $kotlinEnabledInProject")
        val androidBaseBuildModels = project.service<GetAllApplicationBuildModels>()()
        logger.warn("androidBaseBuildModels: " + androidBaseBuildModels.joinToString { it.moduleRootDirectory.name })
        project.service<AddHiltDependenciesToAndroidModules>()()
        val listenableFuture = GradleProjectSystemSyncManager(project).syncProject(ProjectSystemSyncManager.SyncReason.PROJECT_MODIFIED)
        listenableFuture.addListener(
            { addAnnotationToApplicationClasses(project, androidBaseBuildModels, kotlinEnabledInProject) },
            { DumbService.getInstance(project).smartInvokeLater(it) },
        )
    }

    private fun addAnnotationToApplicationClasses(project: Project, androidBaseBuildModels: List<GradleBuildModel>, kotlinEnabledInProject: Boolean) {
        val androidFacets = androidBaseBuildModels.mapNotNull { it.psiElement?.let { psiElement -> AndroidFacet.getInstance(psiElement) } }
        logger.warn("androidFacets: " + androidFacets.joinToString { it.module.name })
        executeCommand {
            runWriteAction {
                androidFacets.forEach { androidFacet ->
                    val primaryManifestXml = androidFacet.getPrimaryManifestXml()
                    val packageName = primaryManifestXml?.packageName
                    val applicationName = primaryManifestXml?.findApplicationName()
                    if (applicationName == null && packageName != null) {
                        val newApplicationName = "GeneratedApplication" // todo: customize name (and package?)
                        generateApplicationFile(project, packageName, newApplicationName, kotlinEnabledInProject).also { applicationFile ->
                            logger.warn("Application Psi File generated, length = ${applicationFile.textLength}")
                            val moduleDir = androidFacet.module.guessModuleDir() ?: return@also
                            val rootDir = PsiManager.getInstance(project).findDirectory(moduleDir)
                                ?.also { logger.warn("Module dir found, name = ${it.name}") }
                                ?.findSubdirectory("src")
                                ?.findSubdirectory("main")
                                ?.let { it.findSubdirectory("kotlin") ?: it.findSubdirectory("java") }
                            var dirToAddAppFile = rootDir
                            packageName.split('.').forEach {
                                dirToAddAppFile = dirToAddAppFile?.findSubdirectory(it)
                            }
                            dirToAddAppFile?.add(applicationFile)
                        }
                        primaryManifestXml.setApplicationName(".$newApplicationName")
                        return@forEach
                    }
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

    private fun AndroidManifestXmlFile.setApplicationName(name: String) {
        accept(object : XmlRecursiveElementVisitor() {
            override fun visitXmlTag(tag: XmlTag?) {
                super.visitXmlTag(tag)
                if ("application" != tag?.name) return
                tag.setAttribute("android:name", name)
            }
        })
    }

    private val freeMarkerConfig by lazy {
        Configuration(Version(Version.intValueFor(2, 3, 31))).apply {
            setClassForTemplateLoading(this@AddHiltAction::class.java, "/templates")
            defaultEncoding = Charsets.UTF_8.name()
            templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER // todo: try without
        }
    }

    private fun generateApplicationFile(
        project: Project,
        packageName: String,
        applicationName: String,
        kotlinEnabledInProject: Boolean,
    ): PsiFile {
        val generationParameters = mapOf<String, Any>(
            "packageName" to packageName,
            "applicationName" to applicationName,
        )
        if (kotlinEnabledInProject) {
            val template = freeMarkerConfig.getTemplate("appFileTemplate.kt.ftl")
            val templateText = StringWriter().use { writer ->
                template.process(generationParameters, writer)
                writer.buffer.toString()
            }
            return PsiFileFactory.getInstance(project).createFileFromText(
                "GeneratedApplication.kt", // todo: what if it already exists?
                KotlinLanguage.INSTANCE,
                templateText,
            )
        } else {
            val template = freeMarkerConfig.getTemplate("appFileTemplate.java.ftl")
            val templateText = StringWriter().use { writer ->
                template.process(generationParameters, writer)
                writer.buffer.toString()
            }
            return PsiFileFactory.getInstance(project).createFileFromText(
                "GeneratedApplication.java", // todo: what if it already exists?
                JavaLanguage.INSTANCE,
                templateText,
            )
        }
    }
}