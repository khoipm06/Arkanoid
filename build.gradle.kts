plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
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
    version = "21.0.5"
    modules = listOf("javafx.controls", "javafx.graphics", "javafx.fxml", "javafx.media")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.json:json:20240303")
//    implementation("org.openjfx:javafx-controls:21")
//    implementation("org.openjfx:javafx-fxml:21")
//    implementation("org.openjfx:javafx-media:21")
}