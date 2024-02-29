plugins {
    id("org.openjfx.javafxplugin") version "0.0.10" // Varmista, että versio on uusin tai sopiva projektisi kannalta
    kotlin("jvm") version "1.9.22"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation("no.tornado:tornadofx:1.7.20")
    implementation ("mysql:mysql-connector-java:8.0.28")

}

javafx {
    version = "17" // Määritä JavaFX:n versio
    modules = listOf("javafx.controls") // Voit lisätä tarvittavia JavaFX-moduuleja tarpeen mukaan
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
