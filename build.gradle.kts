//import com.google.protobuf.gradle.generateProtoTasks
//import com.google.protobuf.gradle.protobuf
//import com.google.protobuf.gradle.protoc
//import org.jetbrains.compose.compose
//import org.jetbrains.compose.desktop.application.dsl.TargetFormat
//import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

//group = "com.ruslanborysov"
//version = "1.0"
//
//plugins {
//    idea
//    kotlin("jvm") version "1.6.10"
//    id("org.jetbrains.compose") version "1.0.1-rc2"
//    id("org.bytedeco.gradle-javacpp-platform") version "1.5.6"
//    id("com.google.protobuf") version "0.8.18"
//}
//
//sourceSets {
//    main {
//        proto {}
//    }
//    test {
//        proto {}
//    }
//}
//
//repositories {
//    jcenter()
//    mavenCentral()
//    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
//    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
//    flatDir { dir("libs") }
//}
//
//ext {
//    set("javacppPlatform", "windows-x86_64")
//}
//
//dependencies {
//    implementation(compose.desktop.currentOs)
////    implementation("com.googlecode.soundlibs:mp3spi:1.9.5.4")
//    implementation("com.cloudburst:java-lame:3.98.4")
////    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")
////    implementation("org.apache.httpcomponents.client5:httpclient5:5.1")
//
//    implementation("org.tensorflow:tensorflow-core-platform:0.3.3")
//    implementation( "com.google.protobuf:protobuf-kotlin:3.19.1")
////    compileOnly("com.google.protobuf:protobuf-gradle-plugin:0.8.16")
//    implementation("libs:Libresample4j-1.0")
//
//    // https://mvnrepository.com/artifact/androidx.datastore/datastore-preferences
//}
//
//tasks.withType<KotlinCompile> {
//    kotlinOptions.jvmTarget = "14"
//}
//tasks.withType<JavaCompile> {
//    options.fork(mapOf(Pair("jvmArgs", listOf("--add-opens", "jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED"))))
//}
//
//
//compose.desktop {
//    application {
//        mainClass = "MainKt"
//        nativeDistributions {
//            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
//            packageName = "AudioClipsEditorApp"
//            packageVersion = "1.0.0"
//            appResourcesRootDir.set(sourceSets.main.get().output.resourcesDir)
//        }
//    }
//}
//
//protobuf {
//    generatedFilesBaseDir = "$projectDir/src"
//    protoc {
//        artifact = "com.google.protobuf:protoc:3.19.1"
//    }
//    generateProtoTasks {
//        all().forEach { task ->
//            task.generateDescriptorSet = true
//            task.builtins {
//                getAt("java").outputSubDir += "/generated"
//                create("kotlin") {
//                    outputSubDir += "/generated"
//                }
//                create("python") {
//                    outputSubDir += "/generated"
//                }
//            }
//        }
//    }
//}

plugins {
    kotlin("jvm") version "1.6.10" apply false
    id("org.jetbrains.compose") version "1.0.1" apply false
    id("org.bytedeco.gradle-javacpp-platform") version "1.5.6" apply false
}

ext {
    set("javacppPlatform", "windows-x86_64")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "14"
}