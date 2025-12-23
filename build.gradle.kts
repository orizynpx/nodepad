plugins {
    java
    application
    // Update to 2.0.0 (Supports Java 21+)
    id("org.javamodularity.moduleplugin") version "2.0.0"

    id("org.openjfx.javafxplugin") version "0.1.0"

    // Update to 3.1.1 (Supports Java 21+)
    id("org.beryx.jlink") version "3.1.1"
}

group = "io.github.orizynpx"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        // Recommend 21 for stability with jpackage
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainModule.set("io.github.orizynpx.nodepad")
    // In modular JavaFX, point to the class extending Application (Main), not Launcher
    mainClass.set("io.github.orizynpx.nodepad.app.Main")
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

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
}

jlink {
    imageZip.set(project.file("${buildDir}/distributions/app-${javafx.platform.classifier}.zip"))
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))

    launcher {
        name = "Nodepad"
    }

    // MERGE LIST:
    // We must merge ALL non-modular jars.
    // 1. SQLite
    // 2. OkHttp + its children (Okio, Kotlin Stdlib)
    // 3. RichTextFX + its children (ReactFX, Flowless, UndoFX, WellBehavedFX)
    forceMerge(
        "sqlite-jdbc",
        "okhttp", "okio", "kotlin",
        "richtextfx", "reactfx", "flowless", "undofx", "wellbehavedfx"
    )

    jpackage {
        installerType = "exe"
        installerOptions.addAll(listOf(
            "--win-per-user-install",
            "--win-dir-chooser",
            "--win-menu",
            "--win-shortcut",
            "--dest", "${buildDir}/installer"
        ))
    }
}