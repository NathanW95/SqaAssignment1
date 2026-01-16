plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
    id("org.springframework.boot") version "4.0.0"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("plugin.jpa") version "2.2.21"
    id("com.diffplug.spotless") version "6.25.0"
    id("info.solidsoft.pitest") version "1.19.0-rc.2"
}

group = "org.assignment"
version = "0.0.1-SNAPSHOT"
description = "SqaAssignment1Blog"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-thymeleaf-test")
    testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

spotless {
    kotlin {
        ktlint("1.4.1")
        trimTrailingWhitespace()
        indentWithSpaces(4)
        endWithNewline()
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        ktlint("1.4.1")
    }
}

pitest {
    junit5PluginVersion.set("1.2.1")
    useClasspathFile.set(true)
    targetClasses.set(listOf("org.assignment.blog.*"))
    excludedClasses.set(
        listOf(
            "org.assignment.blog.SqaAssignment1BlogApplication*",
            "org.assignment.blog.model.*",
        ),
    )
    threads.set(Runtime.getRuntime().availableProcessors())
    outputFormats.set(listOf("HTML", "XML"))
    timestampedReports.set(false)
    reportDir.set(file("$rootDir/build/reports/pitest"))
}

val mutationCoverageThreshold = 70
val shouldFailBuildIfBelowThreshold = false

tasks.register("checkMutationCoverage") {
    dependsOn("pitest")
    doLast {
        val reportDir = file("$buildDir/reports/pitest")
        val xmlFile = reportDir.walk().firstOrNull { it.name == "mutations.xml" }
        var roundedCoverage = 0

        if (xmlFile != null && xmlFile.exists()) {
            val factory =
                javax.xml.parsers.DocumentBuilderFactory
                    .newInstance()
            val builder = factory.newDocumentBuilder()
            val doc = builder.parse(xmlFile)
            val mutations = doc.getElementsByTagName("mutation")
            val totalMutations = mutations.length
            var killedMutations = 0
            for (i in 0 until mutations.length) {
                val mutation = mutations.item(i)
                if (mutation.attributes.getNamedItem("status")?.nodeValue == "KILLED") {
                    killedMutations++
                }
            }
            val coverage = if (totalMutations > 0) (killedMutations * 100.0 / totalMutations) else 0.0
            roundedCoverage = Math.round(coverage).toInt()

            logger.lifecycle("")
            logger.lifecycle("========================================")
            logger.lifecycle("   MUTATION TESTING RESULTS")
            logger.lifecycle("========================================")
            logger.lifecycle("Total Mutations:  $totalMutations")
            logger.lifecycle("Killed Mutations: $killedMutations")
            logger.lifecycle("Coverage:         $roundedCoverage%")
            logger.lifecycle("Threshold:        $mutationCoverageThreshold%")
            logger.lifecycle("========================================")

            if (roundedCoverage >= mutationCoverageThreshold) {
                logger.lifecycle("✅ Mutation coverage PASSED ($roundedCoverage% >= $mutationCoverageThreshold%)")
            } else {
                logger.lifecycle("⚠️  Mutation coverage BELOW threshold ($roundedCoverage% < $mutationCoverageThreshold%)")
            }
            logger.lifecycle("")
        } else {
            logger.lifecycle("⚠️  PITest mutation report not found at: $reportDir")
        }

        if (shouldFailBuildIfBelowThreshold && roundedCoverage < mutationCoverageThreshold) {
            throw GradleException("Mutation coverage is below the threshold of $mutationCoverageThreshold%.")
        }
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
