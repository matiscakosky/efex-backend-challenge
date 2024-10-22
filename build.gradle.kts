plugins {
    id("jacoco")
    id("org.jetbrains.kotlin.jvm") version "1.9.21"
    id("org.jetbrains.kotlin.kapt") version "1.9.21"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.9.22"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.21"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.micronaut.application") version "4.3.5"
    id("org.jmailen.kotlinter") version "4.4.1"
}
version = "0.1"
group = "com.efex"

//region dependencies versions
val kotlinVersion = "1.9.24"
val kotlinxSerializationVersion = "1.7.3"
val logbackClassicVersion = "1.5.10"
val mapstructVersion = "1.6.2"
val micronautVersion = "4.0.0"
val jacksonModuleKotlinVersion = "2.14.2"
//endregion

repositories {
    mavenCentral()
}

sourceSets {
    create("integrationTest") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

val integrationTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.implementation.get(), configurations.testImplementation.get())
}
val integrationTestRuntimeOnly: Configuration by configurations.getting {
    extendsFrom(configurations.runtimeOnly.get(), configurations.testRuntimeOnly.get())
}

micronaut {
    version.set(micronautVersion)
}

dependencies {
    annotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")
    annotationProcessor("io.micronaut:micronaut-inject-java")
    annotationProcessor("io.micronaut.validation:micronaut-validation-processor")
    compileOnly("io.micronaut:micronaut-aop")
    runtimeOnly("org.yaml:snakeyaml")
    kapt("org.mapstruct:mapstruct-processor:$mapstructVersion")
    kapt("io.micronaut:micronaut-http-validation")
    // Required by micronaut4
    implementation("io.micronaut.validation:micronaut-validation")

    // Jackson
    implementation("io.micronaut:micronaut-jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonModuleKotlinVersion")

    // Micronaut
    implementation("io.micronaut:micronaut-management")
    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut:micronaut-runtime")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut.aws:micronaut-aws-sdk-v2")
    implementation("io.micronaut.security:micronaut-security-jwt")
    implementation("io.micronaut.cache:micronaut-cache-caffeine")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")

    // Mapstruct
    implementation("org.mapstruct:mapstruct:$mapstructVersion")

    // Dependency injection
    implementation("jakarta.annotation:jakarta.annotation-api")
    implementation("jakarta.validation:jakarta.validation-api:3.1.0")

    // AWS
    implementation("software.amazon.awssdk:dynamodb")
    implementation("software.amazon.awssdk:dynamodb-enhanced")

    implementation("io.micronaut.reactor:micronaut-reactor")
    implementation("ch.qos.logback:logback-classic:$logbackClassicVersion")

    implementation("io.getunleash:unleash-client-java:9.2.4")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:localstack")
}

application {
    mainClass.set("com.efex.apps.ApplicationKt")
}

kotlin {
    jvmToolchain(17)
}

val cleanYamlDoc = tasks.register("cleanYamlDoc") {
    val inputFile = File("docs/public/v1/swagger.yaml")
    val outputFile = File("docs/public/v1/swagger-bundled.yaml")

    doLast {
        val inputLines = inputFile.readLines()
        val outputLines = mutableListOf<String>()

        for (line in inputLines) {
            if (!line.startsWith("\$ref") && !line.endsWith(".md")) {
                outputLines.add(line)
            }
        }

        outputFile.writeText(outputLines.joinToString(separator = "\n"))
    }
}

val integrationTest = task<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"

    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath

    // required to mock java.time.Instant in integration tests, related MockK issue: https://github.com/mockk/mockk/issues/681
    jvmArgs("--add-opens", "java.base/java.time=ALL-UNNAMED")

    shouldRunAfter("test")
}

val integrationTestReport = task<JacocoReport>("integrationTestReport") {
    description = "Generates coverage for integration tests."
    group = "reporting"

    executionData(integrationTest)
    additionalSourceDirs(project.files(project.sourceSets["main"].allSource.srcDirs))
    additionalClassDirs(project.sourceSets["main"].output)

    mustRunAfter(integrationTest)
    reports {
        html.outputLocation.set(project.reporting.file("jacoco/integrationTest/html"))
        html.required.set(true)
        xml.outputLocation.set(project.reporting.file("jacoco/integrationTest/jacocoTestReport.xml"))
        xml.required.set(true)
    }

    doLast {
        val path = reports.html.outputLocation.get().asFile.toURI().path
        println("See integration report at: file://${path}index.html")
    }
}
integrationTest.finalizedBy(integrationTestReport)
integrationTest.dependsOn(cleanYamlDoc)

val fullReport = task<JacocoReport>("fullReport") {
    description = "Generates coverage for the whole test suites"
    group = "reporting"

    executionData(tasks.test.get(), integrationTest)
    executionData.setFrom(fileTree(buildDir).include("/jacoco/*.exec"))
    additionalClassDirs(files(project.sourceSets.main.get().output))
    additionalSourceDirs(project.files(project.sourceSets.main.get().allSource.srcDirs))

    reports {
        html.required.set(true)
        xml.outputLocation.set(project.reporting.file("jacoco/fullReport/jacocoTestReport.xml"))
        xml.required.set(true)
    }

    doLast {
        val path = reports.html.outputLocation.get().asFile.toURI().path
        println("See full coverage report at: file://${path}index.html")
    }
}

tasks {
    withType(Test::class).configureEach {
        // TODO: Check to use test-suite-plugin -> https://docs.gradle.org/current/userguide/jvm_test_suite_plugin.html#jvm_test_suite_plugin
        // useJUnitPlatform changes when using test-suite-plugin -> https://github.com/gradle/gradle/issues/23544
        useJUnitPlatform()
    }

    lintKotlinMain {
        exclude("**/migrations/**")
    }

    formatKotlinMain {
        exclude("**/migrations/**")
    }

    test {
        finalizedBy(jacocoTestReport)
        maxParallelForks = Runtime.getRuntime().availableProcessors()
        // required to mock java.time.Instant in integration tests, related MockK issue: https://github.com/mockk/mockk/issues/681
        jvmArgs("--add-opens", "java.base/java.time=ALL-UNNAMED")
    }

    jacocoTestReport {
        dependsOn(test)
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }
}

graalvmNative.toolchainDetection.set(false)
micronaut {
    runtime("netty")
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("com.efex.*")
        additionalSourceSets.add(sourceSets.getByName("integrationTest"))
    }
}
