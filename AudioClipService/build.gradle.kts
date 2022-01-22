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
    implementation("org.tensorflow:tensorflow-core-platform:0.3.3")

    implementation("com.laszlosystems:libresample4j:1.0")

    implementation("org.apache.poi:poi:4.1.2")
    implementation("org.apache.poi:poi-ooxml:4.1.2")

    implementation("com.sun.mail:javax.mail:1.6.2")

    implementation(project(":FragmentResolverModelProto"))
    implementation(project(":AudioClipServiceProto"))
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.register<Copy>("fetchModels") {
    group = "build"
    from(project(":FragmentResolverModel").buildDir.resolve("saved_models").resolve("final_model"))
    into(
        sourceSets.main.get().resources.srcDirs.find { it.name == "resources"}!!
            .resolve("models").resolve("model").absolutePath
    )
}

tasks.getByName("processResources") {
    dependsOn("fetchModels")
}

//tasks.withType<KotlinCompile> {
//    kotlinOptions.jvmTarget = "14"
//}