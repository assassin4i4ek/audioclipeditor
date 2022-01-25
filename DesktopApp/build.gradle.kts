import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.utils.loadPropertyFromResources

plugins {
//    java
    kotlin("jvm")
    id("org.jetbrains.compose")
}

group = "com.ruslanborysov"
version = "1.0"

repositories {
    mavenCentral()
    jcenter()
    addAll(project(":AudioClipService").repositories)
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    implementation(compose.desktop.currentOs)
    implementation(project(":AudioClipService"))
}



compose.desktop {
    application {
        mainClass = "MainKt"
        javaHome = System.getProperty("java.home")

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            modules("java.sql")
            packageName = "AudioClipsEditorApp"
            packageVersion = "1.0.0"
            appResourcesRootDir.set(sourceSets.main.get().output.resourcesDir)
        }
    }
}

tasks.getByName<ProcessResources>("processResources") {
//    println()
    project(":AudioClipService").sourceSets.main.get().output
        .resourcesDir?.absolutePath?.let { resourcePath ->
            from(resourcePath) {
                into("common")
            }
        }
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
