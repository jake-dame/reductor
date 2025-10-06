import java.nio.file.Paths

plugins {
    java
}

apply(from = rootProject.file("golden.gradle.kts"))

group = "reductor"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

dependencies {
    implementation("org.audiveris:proxymusic:4.0.3")
    implementation("org.slf4j:slf4j-api:2.1.0-alpha1")
    implementation("ch.qos.logback:logback-classic:1.5.13")

    // Gradle 9.1.0 changed some stuff with how you need to explicitly pull in API + engine,
    // seems they are trying to migrate people to `jvm-test-suite` plug-in, but that isn't stable yet
    // Keeping this here (just from their docu)
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

tasks.named("test") {
    dependsOn("goldenTestUpdate", "deleteMuseScoreBackup")
}

tasks.register<Delete>("cleanOutputs") {
    delete("outputs")
}

val testResourceDir: java.nio.file.Path = Paths.get("src/test/resources")

tasks.register<Delete>("deleteMuseScoreBackup") {
    delete(testResourceDir.resolve(".mscbackup"))
}

