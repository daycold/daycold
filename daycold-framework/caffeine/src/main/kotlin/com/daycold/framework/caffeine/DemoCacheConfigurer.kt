package com.daycold.framework.caffeine

import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.CachingConfigurer
import org.springframework.cache.interceptor.CacheErrorHandler
import org.springframework.cache.interceptor.CacheResolver
import org.springframework.cache.interceptor.KeyGenerator

/**
 * @author : liuwang
 * @date : 2020-10-22T15:50:47Z
 */
class DemoCacheConfigurer(
        private val keyGenerator: KeyGenerator,
        private val cacheResolver: CacheResolver? = null
) : CachingConfigurer {
    private val cacheManager = DemoCacheManager()

    override fun cacheManager(): CacheManager = cacheManager

    override fun keyGenerator(): KeyGenerator? {
        return keyGenerator
    }

    override fun cacheResolver(): CacheResolver? {
        return cacheResolver
    }

    override fun errorHandler(): CacheErrorHandler = cacheManager.errorHandler
}