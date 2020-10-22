package com.daycold.framework.caffeine

import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.CachingConfigurer
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.interceptor.CacheErrorHandler
import org.springframework.cache.interceptor.CacheResolver
import org.springframework.cache.interceptor.KeyGenerator
import org.springframework.context.annotation.Configuration

/**
 * @author : liuwang
 * @date : 2020-10-22T15:50:47Z
 */
@EnableCaching
@Configuration
class DemoCacheConfigurer : CachingConfigurer{
    private val cacheManager = DemoCacheManager()

    override fun cacheManager(): CacheManager = cacheManager

    override fun keyGenerator(): KeyGenerator? {
        return null
    }

    override fun cacheResolver(): CacheResolver? {
        return null
    }

    override fun errorHandler(): CacheErrorHandler = cacheManager.errorHandler
}