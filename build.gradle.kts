plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("com.gradleup.shadow") version "9.2.2"
    id("org.beryx.jlink") version "3.1.3"
}

group = "com.arkanoid"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

application {
    mainClass.set("com.arkanoid.GameApplication")
}

javafx {
    version = "21.0.9"
    modules = listOf("javafx.controls", "javafx.graphics", "javafx.fxml", "javafx.media" )
}

// jlink {
//     options = listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages")
//     launcher {
//         name = "com.arkanoid"
//     }
// }

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.xerial:sqlite-jdbc:3.45.1.0")
    implementation("org.slf4j:slf4j-simple:2.0.13")
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("org.openjfx:javafx-media:21.0.9:win")
    implementation("org.openjfx:javafx-controls:21.0.9:win")
    implementation("org.openjfx:javafx-fxml:21.0.9:win")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
