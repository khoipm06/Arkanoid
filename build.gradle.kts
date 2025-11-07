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

    modules = listOf("javafx.controls", "javafx.graphics", "javafx.fxml", "javafx.media" )

}



dependencies {

    implementation("com.google.code.gson:gson:2.10.1")

    implementation("org.json:json:20240303")

    implementation("org.xerial:sqlite-jdbc:3.45.1.0")



    // JUnit 5 Test dependencies

    testImplementation(platform("org.junit:junit-bom:5.10.0"))

    testImplementation("org.junit.jupiter:junit-jupiter")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")



    // JavaFX dependencies for tests

    testImplementation("org.openjfx:javafx-controls:21.0.5")

    testImplementation("org.openjfx:javafx-fxml:21.0.5")

    testImplementation("org.openjfx:javafx-graphics:21.0.5")

}


tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

sourceSets {
    test {
        java {
            srcDirs("src/test/java")
        }
        resources {
            srcDirs("src/test/resources")
        }
    }
}