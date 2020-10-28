dependencies {
    runtimeOnly("io.github.openfeign:feign-core")
    runtimeOnly("javax.servlet:javax.servlet-api")
    compileOnly("org.slf4j:slf4j-api")
    runtimeOnly("org.springframework:spring-messaging")
    runtimeOnly("org.springframework:spring-webmvc")
    runtimeOnly("org.springframework:spring-webflux")
    compileOnly("org.springframework.boot:spring-boot-autoconfigure")
    runtimeOnly("org.springframework.integration:spring-integration-core")
}