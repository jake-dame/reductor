plugins {
    id("java")
}

group = "reductor"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.audiveris:proxymusic:4.0.3")
    implementation("org.slf4j:slf4j-api:2.1.0-alpha1")
    implementation("ch.qos.logback:logback-classic:1.5.12")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}