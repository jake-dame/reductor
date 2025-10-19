import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import java.nio.file.Paths

group = "reductor"
version = "1.0-SNAPSHOT"

plugins {
    java
    application
}

repositories {
    mavenCentral()
}

dependencies {
    // ProxyMusic
    implementation("org.audiveris:proxymusic:4.0.3")

    // SLF4J
    implementation("org.slf4j:slf4j-api:2.1.0-alpha1")

    // LogBack
    implementation("ch.qos.logback:logback-classic:1.5.13")

    // Gradle 9.1.0 changed some stuff with how you need to explicitly pull in API + engine,
    // seems they are trying to migrate people to `jvm-test-suite` plug-in, but that isn't stable yet
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")


    // PBT library, `Arbitraries` and `Combinator` classes
    testImplementation("net.jqwik:jqwik:1.9.3")
    testRuntimeOnly("net.jqwik:jqwik-engine:1.9.3")

    // Fluent assertions library
    testImplementation("org.assertj:assertj-core:3.27.6")
}

// ======================================== CUSTOM TASKS ======================================== //


apply(from = rootProject.file("golden.gradle.kts"))


application { mainClass = "reductor.Main" }

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(25)) }
}

tasks.wrapper {
    gradleVersion = "9.1.0"
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
