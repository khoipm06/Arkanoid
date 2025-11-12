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
    modules = listOf("javafx.controls", "javafx.graphics", "javafx.fxml", "javafx.media", "javafx.swing" )
}

// jlink {
//     options = listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages")
//     launcher {
//         name = "com.arkanoid.GameApplication"
//     }
// }

dependencies {
    implementation("com.google.code.gson:gson:2.13.2")
    implementation("org.xerial:sqlite-jdbc:3.51.0.0")
    implementation("ch.qos.logback:logback-classic:1.5.21")
    
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("org.openjfx:javafx-media:21.0.9")
    implementation("org.openjfx:javafx-controls:21.0.9")
    implementation("org.openjfx:javafx-fxml:21.0.9")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
