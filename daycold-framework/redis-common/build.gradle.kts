dependencies {
    compileOnly("redis.clients:jedis")
    compileOnly("org.springframework:spring-context")
    compileOnly("org.springframework:spring-beans")
    compileOnly("org.springframework.boot:spring-boot-autoconfigure")
    runtimeOnly("org.springframework.data:spring-data-redis")
    compileOnly("org.slf4j:slf4j-api")
}