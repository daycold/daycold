package com.daycold.framework.caffeine

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.Cache
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.cache.interceptor.CacheErrorHandler
import java.time.Duration

/**
 *
 * cacheName 包含 write:xxx 时，设置过期类型为写入后过期，过期时间为 xxx s
 * cacheName 包含 read:xxx 时，设置过期类型为读取后过期，过期时间为 xxx s
 * cacheName 包含 nullable:xxx 时，设置是否缓值为 null 的对象
 * cacheName 包含 ignoreNull:xxx 时，设置缓存值为 null 是否报错，为 false 时报错，为 true 时忽略空值，仅在 nullable 为 false 时生效
 * 默认过期类型为写入后过期, 默认 nullable 为 true，默认 ignoreNull 为 false，默认过期时间为 60 s
 *
 * @author : liuwang
 * @date : 2020-10-22T15:15:28Z
 */
class DemoCacheManager : CaffeineCacheManager() {
    private val cache = mutableMapOf<String, Caffeine<Any, Any>>()
    private val configMap = mutableMapOf<String, CacheConfig>()
    private val nameConfigMap = mutableMapOf<String, String>()

    val errorHandler: CacheErrorHandler = DemoCacheErrorHandler()

    override fun createNativeCaffeineCache(name: String): com.github.benmanes.caffeine.cache.Cache<Any, Any> {
        return getBuilder(name).build()
    }

    override fun createCaffeineCache(name: String): Cache {
        val cache = createNativeCaffeineCache(name)
        val key = nameConfigMap[name]
        val config = if (key == null) null else configMap[key]
        return CaffeineCache(name, cache, config?.allowNullValues ?: false)
    }

    private fun getBuilder(config: CacheConfig): Caffeine<Any, Any> {
        val caffeine = Caffeine.newBuilder()
        if (config.expireAfterRead) {
            caffeine.expireAfterAccess(Duration.ofSeconds(config.expireSeconds))
        } else {
            caffeine.expireAfterWrite(Duration.ofSeconds(config.expireSeconds))
        }
        return caffeine
    }

    private fun getBuilder(name: String): Caffeine<Any, Any> {
        val key = nameConfigMap[name]
        if (key != null) {
            val caffeine = cache[key]
            if (caffeine != null) {
                return caffeine;
            }
        }
        val names = name.split("\\s+")
        var expireAfterRead = DEFAULT_EXPIRE_AFTER_READ
        var allowNullValues = DEFAULT_ALLOW_NULL_VALUES
        var ignoreNullValues = DEFAULT_IGNORE_NULL_VALUES
        var expireSeconds = DEFAULT_EXPIRE_SECONDS
        for (split in names) {
            when {
                split.startsWith("read:") -> {
                    expireAfterRead = true
                    expireSeconds = split.substring(5).toLong()
                }
                split.startsWith("write:") -> {
                    expireAfterRead = false
                    expireSeconds = split.substring(6).toLong()
                }
                split.startsWith("nullable:") ->
                    allowNullValues = split.substring(9).toBoolean()
                split.startsWith("ignoreNull:") ->
                    ignoreNullValues = split.substring(11).toBoolean()
            }
        }
        val newKey = toConfigName(allowNullValues, ignoreNullValues, expireAfterRead, expireSeconds)
        val config = configMap[key]
        val cacheConfig = if (config == null) {
            val newConfig = CacheConfig(expireSeconds, allowNullValues, expireAfterRead, ignoreNullValues)
            configMap[newKey] = newConfig
            newConfig
        } else config
        nameConfigMap[name] = newKey
        val caffeine = cache[newKey]
        if (caffeine == null) {
            val newCaffeine = getBuilder(cacheConfig)
            cache[newKey] = newCaffeine
            return newCaffeine
        }
        return caffeine
    }

    private inner class DemoCacheErrorHandler : CacheErrorHandler {
        override fun handleCacheGetError(exception: RuntimeException, cache: Cache, key: Any) {
            throw exception
        }

        override fun handleCacheClearError(exception: RuntimeException, cache: Cache) {
            throw exception
        }

        override fun handleCachePutError(exception: RuntimeException, cache: Cache, key: Any, value: Any?) {
            if (value == null) {
                val name = cache.name ?: throw exception
                val config = configMap[name] ?: throw exception
                if (config.ignoreNullValues) {
                    return
                }
            }
            throw exception
        }

        override fun handleCacheEvictError(exception: RuntimeException, cache: Cache, key: Any) {
            throw exception
        }
    }
}

private data class CacheConfig(
        val expireSeconds: Long,
        val allowNullValues: Boolean,
        val expireAfterRead: Boolean,
        val ignoreNullValues: Boolean
) {
    override fun toString() = toConfigName(allowNullValues, ignoreNullValues, expireAfterRead, expireSeconds)
}

private const val DEFAULT_EXPIRE_SECONDS = 60L;
private const val DEFAULT_ALLOW_NULL_VALUES = true;
private const val DEFAULT_EXPIRE_AFTER_READ = false;
private const val DEFAULT_IGNORE_NULL_VALUES = false;

private fun toConfigName(allowNullValues: Boolean, ignoreNullValues: Boolean, expireAfterRead: Boolean, expireSeconds: Long) = "$allowNullValues:$ignoreNullValues:$expireAfterRead:$expireSeconds"
