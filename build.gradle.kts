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
}
