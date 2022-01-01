import com.google.protobuf.gradle.protobuf

group = "com.ruslanborysov"
version = "1.0"

plugins {
    java
    id("com.google.protobuf")
}

dependencies {
    protobuf(project(":FragmentResolverModelProto"))
}

//tasks.register("build") {
//    dependsOn(project(":FragmentResolverModelProto").tasks.getByName("generateProto"))
//}