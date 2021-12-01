import com.google.protobuf.gradle.*
import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File

group = "com.ruslanborysov"
version = "1.0"

plugins {
    idea
    kotlin("jvm") version "1.5.10"
    id("org.jetbrains.compose") version "0.5.0-build228"
    kotlin("plugin.serialization") version "1.5.10"
    id("org.bytedeco.gradle-javacpp-platform") version "1.5.6"
    id("com.google.protobuf") version "0.8.16"
}

idea {
    module {
        sourceDirs.add(file("src/main/proto"))
        testSourceDirs.add(file("src/main/proto"))
    }
}

repositories {
    jcenter()
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
    }
    flatDir {
        dir("libs")
    }
}

ext {
    set("javacppPlatform", "windows-x86_64")
}

dependencies {
    implementation(compose.desktop.currentOs)
//    implementation("com.googlecode.soundlibs:mp3spi:1.9.5.4")
    implementation("com.cloudburst:java-lame:3.98.4")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")
    implementation("org.apache.httpcomponents.client5:httpclient5:5.1")
    implementation("org.tensorflow:tensorflow-core-platform:0.3.3")
//    implementation("org.tensorflow:tensorflow-core-platform:0.4.0-SNAPSHOT")

    implementation( "com.google.protobuf:protobuf-kotlin:3.17.3")
    compileOnly("com.google.protobuf:protobuf-gradle-plugin:0.8.16")
    implementation("libs:Libresample4j-1.0")

    // https://mvnrepository.com/artifact/androidx.datastore/datastore-preferences
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "14"
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "AudioClipsEditorApp"
            packageVersion = "1.0.0"
        }
    }
}

protobuf {
    generatedFilesBaseDir = "$projectDir/src"

    protoc {
        artifact = "com.google.protobuf:protoc:3.17.3"
    }
    generateProtoTasks {
        all().forEach { task ->
            task.generateDescriptorSet = true
            task.builtins {
                getAt("java").outputSubDir += "/generated"
                create("kotlin") {
                    outputSubDir += "/generated"
                }
                create("python") {
                    outputSubDir += "/generated"
                }
            }
        }
    }
}