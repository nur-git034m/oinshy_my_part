import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    dependencies {
        classpath("com.google.cloud.tools:jib-spring-boot-extension-gradle:0.1.0")
    }
}

plugins {
    id("org.springframework.boot") version "2.3.4.RELEASE"
    id("io.spring.dependency-management") version "1.0.10.RELEASE"
    id("com.google.cloud.tools.jib") version "2.7.0"
    kotlin("jvm") version "1.3.72"
    kotlin("plugin.spring") version "1.3.72"
    kotlin("plugin.jpa") version "1.3.72"
}

group = "kz.toyville"
version = System.getenv("TOYVILLE_BACK_VERSION")
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

//extra["testcontainersVersion"] = "1.14.3"

dependencies {
    implementation(platform("software.amazon.awssdk:bom:2.15.20"))
    implementation("software.amazon.awssdk:s3")

//	implementation("org.springframework.boot:spring-boot-starter-batch")
//	implementation("org.springframework.boot:spring-boot-starter-data-elasticsearch")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-rest")
    implementation("org.springframework.boot:spring-boot-starter-validation")
//	implementation("org.springframework.boot:spring-boot-starter-integration")
	implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
//	implementation("org.flywaydb:flyway-core")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        exclude(module = "junit")
        exclude(module = "mockito-core")
    }
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("com.ninja-squad:springmockk:2.0.3")
//	testImplementation("org.springframework.batch:spring-batch-test")
//	testImplementation("org.springframework.integration:spring-integration-test")
    testImplementation("org.springframework.security:spring-security-test")
//	testImplementation("org.testcontainers:elasticsearch")
//	testImplementation("org.testcontainers:junit-jupiter")
//	testImplementation("org.testcontainers:postgresql")

    testRuntimeOnly("com.h2database:h2")
}

//dependencyManagement {
//	imports {
//		mavenBom("org.testcontainers:testcontainers-bom:${property("testcontainersVersion")}")
//	}
//}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        @Suppress("SpellCheckingInspection")
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

jib {
    from {
        image = "reg.toyville.kz/openjdk-base@sha256:c0c7e09edf9665a926a6ab27891f637aa33e3e01b32998031f1521447d0fa2b8"
    }
    to {
        image = "reg.toyville.kz/back"
        tags = setOf("$version")
    }
    container {
        creationTime = "USE_CURRENT_TIMESTAMP"
        user = "toyville"
    }
    pluginExtensions {
        pluginExtension {
            implementation = "com.google.cloud.tools.jib.gradle.extension.springboot.JibSpringBootExtension"
        }
    }
}

tasks {
    build {
        dependsOn(jib)
    }
}
