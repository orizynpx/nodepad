plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.0.13"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "io.github.orizynpx"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass.set("io.github.orizynpx.nodepad.app.Launcher")
}

javafx {
    version = "21.0.6"
    modules = listOf("javafx.controls", "javafx.fxml")
}

dependencies {
    implementation("org.xerial:sqlite-jdbc:3.45.1.0")
    implementation("org.fxmisc.richtext:richtextfx:0.11.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.22")
}

// SIMPLIFIED CONFIGURATION
// We configure the standard JAR task. The Shadow plugin inherits this manifest.
tasks.jar {
    manifest {
        attributes["Main-Class"] = "io.github.orizynpx.nodepad.app.Launcher"
    }
}