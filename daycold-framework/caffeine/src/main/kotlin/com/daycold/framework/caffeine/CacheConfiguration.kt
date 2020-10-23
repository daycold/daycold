package com.daycold.framework.caffeine

import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.cache.annotation.CachingConfigurer
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.interceptor.CacheResolver
import org.springframework.cache.interceptor.KeyGenerator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * @author : liuwang
 * @date : 2020-10-23T10:28:26Z
 */
@EnableCaching
@Configuration
class CacheConfiguration {
    @Bean
    @ConditionalOnMissingBean(CachingConfigurer::class)
    fun cachingConfigurer(keyGenerator: KeyGenerator, cacheResolverProvider: ObjectProvider<CacheResolver>): CachingConfigurer = DemoCacheConfigurer(keyGenerator, cacheResolverProvider.getIfAvailable() { null })

    @Bean
    @ConditionalOnMissingBean
    fun keyGenerator(): KeyGenerator = DemoKeyGenerator()

    @Bean
    @ConditionalOnProperty(name = ["daycold.caffeine.batch.enabled"], havingValue = "true")
    fun cacheBatch() = CacheBatchAspect()
}