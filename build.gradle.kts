plugins {
    kotlin("jvm") version "1.5.10"
    id("org.jetbrains.intellij") version "1.2.0"
    java
}

group = "com.sechkarev"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.freemarker:freemarker:2.3.31")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.5.31")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version.set("2021.2.2")
    plugins.set(listOf("android", "Groovy", "Kotlin"))
}
tasks {
    patchPluginXml {
        changeNotes.set("""
            Add change notes here.<br>
            <em>most HTML tags may be used</em>        """.trimIndent())
    }
}
tasks.getByName<Test>("test") {
    useJUnitPlatform()
}