plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
}

javafx {
    version = "24"
    modules = listOf("javafx.controls", "javafx.fxml")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    // JavaFX dependencies for a modern desktop UI
    implementation("com.google.code.gson:gson:2.10.1")
}

application {
    mainClass.set("oop.tegevusteplaneerija.client.MainClient")
    applicationDefaultJvmArgs = listOf("--add-opens=java.base/java.time=ALL-UNNAMED")
}