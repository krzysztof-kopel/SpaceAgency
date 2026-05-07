plugins {
    kotlin("jvm") version "2.3.20"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("dev.kourier:amqp-client:0.4.3")
    implementation("org.slf4j:slf4j-simple:2.0.12")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}