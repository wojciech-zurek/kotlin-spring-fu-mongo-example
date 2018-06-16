import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val coroutinesVersion = "0.22.5"
val kotlinVersion = "1.2.50"
val reactorVersion = "Californium-BUILD-SNAPSHOT"
val springVersion = "5.1.0.BUILD-SNAPSHOT"
val springBootVersion = "2.0.2.RELEASE"
val springFuVersion = "1.0.0.BUILD-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
    maven("https://repo.spring.io/libs-release")
    maven("https://repo.spring.io/libs-milestone")
    maven("https://repo.spring.io/libs-snapshot")
    maven("https://repo.spring.io/snapshot")
    maven("https://repo.spring.io/milestone")
}


plugins {
    kotlin("jvm").version("1.2.50")
    application
    id("io.spring.dependency-management").version("1.0.5.RELEASE")
    id("com.github.johnrengelman.shadow").version("2.0.4")
}

application {
    mainClassName = "eu.wojciechzurek.example.ApplicationKt"
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}
tasks.withType<Test> {
    useJUnitPlatform()

}

dependencies {
    implementation("org.springframework.fu:spring-fu:$springFuVersion")
    implementation("org.springframework.fu.module:spring-fu-jackson:$springFuVersion")
    implementation("org.springframework.fu.module:spring-fu-logging:$springFuVersion")
    implementation("org.springframework.fu.module:spring-fu-mongodb:$springFuVersion")
    implementation("org.springframework.fu.module:spring-fu-mustache:$springFuVersion")
    implementation("org.springframework.fu.module:spring-fu-webflux-netty:$springFuVersion")

    testImplementation("org.springframework.fu.module:spring-fu-test:$springFuVersion")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:$springBootVersion") {
            bomProperty("spring.version", springVersion)
            bomProperty("reactor-bom.version", reactorVersion)
        }
    }

    dependencies {
        dependency("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
        dependency("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$coroutinesVersion")
    }
}
