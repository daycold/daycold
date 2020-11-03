plugins {
    kotlin("jvm") version "1.4.10"
}
buildscript {
    repositories {
        maven(url = "https://maven.aliyun.com/repository/gradle-plugin/")
        maven(url = "https://maven.aliyun.com/repository/spring-plugin/")
        maven(url = "https://maven.aliyun.com/repository/public/")
    }
    dependencies {
        classpath("org.springframework.boot:spring-boot-gradle-plugin:${project.extra["springBootVersion"]}")
        classpath("io.spring.gradle:dependency-management-plugin:1.0.10.RELEASE")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${project.extra["kotlinVersion"]}")
        classpath("org.jetbrains.kotlin:kotlin-allopen:${project.extra["kotlinVersion"]}")
    }
}

repositories {
    maven(url = "http://maven.aliyun.com/repository/public/")
    mavenCentral()
}

subprojects {
    apply(from = "${rootProject.rootDir}/gradle/default_project_config.gradle.kts")
}
