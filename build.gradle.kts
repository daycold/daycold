plugins {
    kotlin("jvm") version "1.4.10"
}
buildscript {
    repositories {
        maven(url = "http://maven.aliyun.com/nexus/content/groups/public/")
    }
    dependencies {
        classpath("org.springframework.boot:spring-boot-gradle-plugin:${project.extra["springBootVersion"]}")
        classpath("io.spring.gradle:dependency-management-plugin:1.0.3.RELEASE")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${project.extra["kotlinVersion"]}")
        classpath("org.jetbrains.kotlin:kotlin-allopen:${project.extra["kotlinVersion"]}")
    }
}

repositories {
    maven(url = "http://maven.aliyun.com/nexus/content/groups/public/")
    mavenCentral()
}

subprojects {
    apply(from = "${rootProject.rootDir}/gradle/default_project_config.gradle.kts")
}
