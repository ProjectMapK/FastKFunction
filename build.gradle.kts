plugins {
    kotlin("jvm") version "1.4.10"
}

group = "com.mapk"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    // https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter
    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter", version = "5.7.0") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
}

tasks {
    test {
        useJUnitPlatform()
    }
}
