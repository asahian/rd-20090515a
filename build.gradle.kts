import org.gradle.api.file.DuplicatesStrategy

plugins {
    id("java-library")
}

group = "net.minecraft"
version = "rd-20090515"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

val natives: Configuration by configurations.creating
natives.isTransitive = true

dependencies {
    implementation(group = "org.lwjgl.lwjgl", name = "lwjgl", version = "2.9.3")
    implementation(group = "org.lwjgl.lwjgl", name = "lwjgl_util", version = "2.9.3")
    natives(group = "org.lwjgl.lwjgl", name = "lwjgl-platform", version = "2.9.3", classifier = "natives-windows")
    natives(group = "org.lwjgl.lwjgl", name = "lwjgl-platform", version = "2.9.3", classifier = "natives-linux")
    natives(group = "org.lwjgl.lwjgl", name = "lwjgl-platform", version = "2.9.3", classifier = "natives-osx")
}


tasks.register<JavaExec>("run") {
    jvmArgs = listOf("-Dorg.lwjgl.librarypath=${project.projectDir.toPath()}/run/natives")
    mainClass.set("com.mojang.rubydung.RubyDung")
    classpath = sourceSets.getByName("main").runtimeClasspath
    workingDir = project.projectDir.resolve("run")
    dependsOn(tasks.named("extractNatives"))
}

tasks.register<Copy>("extractNatives") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    dependsOn(natives)
    from(natives.map { zipTree(it) })
    into(project.projectDir.resolve("run/natives"))
}