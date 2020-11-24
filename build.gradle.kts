plugins {
    kotlin("jvm") version "1.4.20"
    id("org.jlleitschuh.gradle.ktlint") version "9.4.1"
    id("jacoco")
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
    compileKotlin {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
    compileTestKotlin {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "1.8"
        }
    }
    jacocoTestReport {
        reports {
            xml.isEnabled = true
            csv.isEnabled = false
            html.isEnabled = true
        }
    }
    test {
        useJUnitPlatform()
        // テスト終了時にjacocoのレポートを生成する
        finalizedBy(jacocoTestReport)
    }
}
