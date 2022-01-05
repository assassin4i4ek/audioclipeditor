plugins {
    kotlin("jvm")
    id("org.bytedeco.gradle-javacpp-platform")
}

repositories {
    mavenCentral()
    jcenter()
    flatDir {
        dirs("$projectDir/libs")
    }
}

group = "com.ruslanborysov"
version = "1.0"

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    implementation("com.cloudburst:java-lame:3.98.4")
    implementation("com.laszlosystems:libresample4j:1.0")
    implementation("org.tensorflow:tensorflow-core-platform:0.3.3")
    implementation(project(":FragmentResolverModelProto"))
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.register<Copy>("fetchModels") {
    group = "build"
    println(project(":FragmentResolverModel").buildDir)
    from(project(":FragmentResolverModel").buildDir.resolve("saved_models"))
    into(
        sourceSets.main.get().resources.srcDirs.find { it.name == "resources"}!!
            .resolve("models").absolutePath
    )
}

tasks.getByName("processResources") {
    dependsOn("fetchModels")
}

//tasks.withType<KotlinCompile> {
//    kotlinOptions.jvmTarget = "14"
//}