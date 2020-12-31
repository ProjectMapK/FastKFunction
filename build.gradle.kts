plugins {
    id("maven")
    kotlin("jvm") version "1.4.21"
    // プロダクションコード以外
    id("org.jlleitschuh.gradle.ktlint") version "9.4.1"
    id("jacoco")
    id("me.champeau.gradle.jmh") version "0.5.2"
}

group = "com.mapk"
version = "0.1.0"

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
    testImplementation("io.mockk:mockk:1.10.3-jdk8")

    jmhImplementation(group = "org.openjdk.jmh", name = "jmh-core", version = "1.26")
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
    // https://qiita.com/wrongwrong/items/16fa10a7f78a31830ed8
    jmhJar {
        exclude("META-INF/versions/9/module-info.class")
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

jmh {
    fork = 3
    iterations = 3
    threads = 3
    warmupBatchSize = 3
    warmupIterations = 3

    failOnError = true
    isIncludeTests = false

    resultFormat = "CSV"
}
