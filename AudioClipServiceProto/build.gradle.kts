import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc

plugins {
    kotlin("jvm")
    idea
    id("com.google.protobuf")
}

group = "com.ruslanborysov"
version = "1.0"

sourceSets {
    main {
        proto {}
    }
    test {
        proto {}
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    api( "com.google.protobuf:protobuf-kotlin:3.19.1")
    api( "com.google.protobuf:protobuf-java-util:3.19.1")
//    compileOnly("com.google.protobuf:protoc:3.19.1")
}

protobuf {
//    generatedFilesBaseDir = "$projectDir/src"
    protoc {
        artifact = "com.google.protobuf:protoc:3.19.1"
    }
    generateProtoTasks {
        all().forEach { task ->
//            task.generateDescriptorSet = true
            task.builtins {
//                getAt("java").outputSubDir += "/generated"
                create("kotlin") {
//                    outputSubDir += "/generated"
                }
//                create("python") {
//                    outputSubDir += "/generated"
//                }
            }
        }
    }
}

tasks.withType<JavaCompile> {
    options.fork(mapOf(Pair("jvmArgs", listOf("--add-opens", "jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED"))))
}