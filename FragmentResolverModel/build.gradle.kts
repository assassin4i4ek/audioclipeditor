import com.google.protobuf.gradle.protobuf
import groovy.lang.MetaClass

group = "com.ruslanborysov"
version = "1.0"

buildDir = projectDir.resolve("results")

plugins {
    id("ru.vyarus.use-python") apply true
}

python {
    pip("jupyter:1.0.0")
    pip("scipy:1.7.1")
    pip("matplotlib:3.5.1")

    pip("protobuf:3.19.1")

    pip("keras:2.6.0")
    pip("tensorflow:2.6.0")
    pip("tensorflow-io:0.20.0")
    pip("kapre:0.3.5")
}

tasks.register<Copy>("fetchProto") {
    group = "python"
    dependsOn(tasks.getByPath(":FragmentResolverModelProto:generateProto"))
    val protoOutputDir = project(":FragmentResolverModelProto")
        .buildDir.resolve("generated/source/proto/main")
    from(protoOutputDir) {
        include("**.desc")
    }
    from(protoOutputDir.resolve("python"))
    into("src/generated")
}

tasks.register<Exec>("runJupyter") {
    group = "python"
    environment()
    workingDir("src")
    executable("jupyter-notebook.exe")
}

