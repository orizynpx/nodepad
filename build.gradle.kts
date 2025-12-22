plugins {
    java
    application
    id("org.javamodularity.moduleplugin") version "1.8.15"
    id("org.openjfx.javafxplugin") version "0.0.13"
    id("org.beryx.jlink") version "2.25.0"
}

group = "io.github.orizynpx"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val junitVersion = "5.12.1"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(24)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

application {
    mainModule.set("io.github.orizynpx.nodepad")
    mainClass.set("io.github.orizynpx.nodepad.app.Launcher")
}

javafx {
    version = "21.0.6"
    modules = listOf("javafx.controls", "javafx.fxml")
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
    implementation("org.xerial:sqlite-jdbc:3.45.1.0")
    implementation("org.fxmisc.richtext:richtextfx:0.11.0")
    // JSON Parsing (This is what you are missing)
    implementation("com.google.code.gson:gson:2.10.1")

    // Network
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
