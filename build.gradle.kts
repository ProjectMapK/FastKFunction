plugins {
    id("maven")
    kotlin("jvm") version "1.4.21"
    // プロダクションコード以外
    id("org.jlleitschuh.gradle.ktlint") version "9.4.1"
    id("jacoco")
    id("me.champeau.gradle.jmh") version "0.5.2"
}

group = "com.mapk"
version = "0.1.3"

repositories {
    mavenCentral()
}

dependencies {
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
    warmupForks = 2
    warmupBatchSize = 3
    warmupIterations = 3
    warmup = "1s"

    fork = 2
    batchSize = 3
    iterations = 2
    timeOnIteration = "1500ms"

    failOnError = true
    isIncludeTests = false

    resultFormat = "CSV"
}
