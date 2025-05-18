import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    extra.apply {
    }
}

plugins {
    val kotlinVersion = "1.9.24"

    id("org.springframework.boot") version "3.3.1"
    id("io.spring.dependency-management") version "1.1.5"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    kotlin("plugin.jpa") version kotlinVersion
    kotlin("plugin.allopen") version kotlinVersion
    kotlin("kapt") version kotlinVersion

    idea
}

group = "com.hero"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

idea {
    module {
        val kaptMain = file("build/generated/source/kapt/main")
        sourceDirs.add(kaptMain)
        generatedSourceDirs.add(kaptMain)
    }
}

/**
 * https://kotlinlang.org/docs/reference/compiler-plugins.html#spring-support
 * automatically supported annotation
 * @Component, @Async, @Transactional, @Cacheable, @SpringBootTest,
 * @Configuration, @Controller, @RestController, @Service, @Repository.
 * jpa meta-annotations not automatically opened through the default settings of the plugin.spring
 */
allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

dependencies {
    /** spring starter */
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-integration")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    kapt("org.springframework.boot:spring-boot-configuration-processor")

    /** kotlin */
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    /** arrow-kt */
    implementation("io.arrow-kt:arrow-core:${DependencyVersion.ARROW_FX}")
    implementation("io.arrow-kt:arrow-fx-coroutines:${DependencyVersion.ARROW_FX}")
    implementation("io.arrow-kt:arrow-fx-stm:${DependencyVersion.ARROW_FX}")

    /** logger */
    implementation("io.github.oshai:kotlin-logging-jvm:${DependencyVersion.KOTLIN_LOGGING}")
    implementation("net.logstash.logback:logstash-logback-encoder:${DependencyVersion.LOGBACK_ENCODER}")

    /** database */
    runtimeOnly("com.mysql:mysql-connector-j")

    /** etc */
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    val isMacOS = System.getProperty("os.name").startsWith("Mac OS X")
    val architecture = System.getProperty("os.arch").lowercase()
    if (isMacOS && architecture == "aarch64") {
        developmentOnly("io.netty:netty-resolver-dns-native-macos:${DependencyVersion.MAC_DNS}:osx-aarch_64")
        testImplementation("io.netty:netty-resolver-dns-native-macos:${DependencyVersion.MAC_DNS}:osx-aarch_64")
    }
}

defaultTasks("bootRun")

springBoot.buildInfo { properties { } }

configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}

tasks.getByName<Jar>("jar") {
    enabled = false
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.withType<Wrapper> {
    gradleVersion = "8.7"
}

tasks.withType<Test> {
    useJUnitPlatform()
}
