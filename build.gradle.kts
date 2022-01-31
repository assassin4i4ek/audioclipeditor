plugins {
    kotlin("jvm") version "1.6.10" apply false
    id("org.jetbrains.compose") version "1.0.1" apply false
    id("org.bytedeco.gradle-javacpp-platform") version "1.5.6" apply false
    id("com.google.protobuf") version "0.8.18" apply false
    id("ru.vyarus.use-python") version "2.3.0" apply false
}

ext {
    set("javacppPlatform", "windows-x86_64")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "14"
}