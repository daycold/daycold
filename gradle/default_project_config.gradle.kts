import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

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
}

apply(plugin = "java")
apply(plugin = "kotlin")
apply(plugin = "kotlin-spring")
apply(plugin = "org.springframework.boot")
apply(plugin = "io.spring.dependency-management")
apply(from = "${rootProject.rootDir}/gradle/dependency.gradle")

tasks.named<Jar>("jar") { 
    enabled = true 
}

tasks.named<BootJar>("bootJar") {
    archiveClassifier.set("boot")
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"

    options.encoding = "UTF-8"
    options.isFork = true
    options.forkOptions.executable = "javac"
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}
