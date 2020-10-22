package com.daycold.framework.redis.common.cache

import com.daycold.framework.redis.common.utils.DateUtil
import java.util.*
import java.util.function.Function
import java.util.stream.Collectors

/**
 * @author : liuwang
 * @date : 2020-08-20T18:14:30Z
 */
abstract class AbstractHashCacheService<T> protected constructor(
        private val expireSeconds: Int = DateUtil.SECONDS_PER_DAY
) : AbstractCacheService(), HashCacheService<T> {
    override fun findAll(key: String): Map<String, T> {
        val topKey = appendKey(key)
        val cachedValue: Map<String, String?> = redisService.hgetAll(topKey)
        if (cachedValue.isEmpty()) {
            return emptyMap()
        }
        return if (cachedValue[NULL_PLACEHOLDER] != null) {
            emptyMap()
        } else cachedValue.keys.stream().collect(Collectors.toMap(Function.identity(),
                { cachedKey: String -> deserialize(cachedValue[cachedKey]) }))
    }

    override fun findOrCreateAll(key: String): Map<String, T> {
        val topKey = appendKey(key)
        val cachedValue: Map<String, String?> = redisService.hgetAll(topKey)
        if (cachedValue.isEmpty()) {
            return saveAll(key)
        }
        return if (cachedValue[NULL_PLACEHOLDER] != null) {
            emptyMap()
        } else cachedValue.keys.stream().collect(Collectors.toMap(Function.identity(),
                { cachedKey: String -> deserialize(cachedValue[cachedKey]) }))
    }

    override fun find(topKey: String, secondKey: String): T? {
        val topCacheKey = appendKey(topKey)
        val secondCacheKey = appendSecondKey(secondKey)
        val cachedValue: String? = redisService.hget(topCacheKey, secondCacheKey)
        return if (cachedValue == null || cachedValue == NULL_PLACEHOLDER) {
            null
        } else deserialize(cachedValue)
    }

    override fun findOrCreate(topKey: String, secondKey: String): T? {
        val topCacheKey = appendKey(topKey)
        val secondCacheKey = appendSecondKey(secondKey)
        val cachedValue: String? = redisService.hget(topCacheKey, secondCacheKey)
        return when (cachedValue) {
            null -> add(topKey, secondKey)
            NULL_PLACEHOLDER -> null
            else -> deserialize(cachedValue)
        }
    }

    override fun delete(topKeys: Collection<String>): Long {
        val topCacheKeys = topKeys.stream().distinct().map { key: String? -> appendKey(key!!) }.collect(Collectors.toList())
        return redisService.del(*topCacheKeys.toTypedArray())
    }

    override fun remove(topKey: String, secondKeys: Collection<String>): Long {
        val topCacheKey = appendKey(topKey)
        val secondCacheKeys = secondKeys.stream().distinct().map { key: String? -> appendSecondKey(key) }.collect(Collectors.toList())
        return redisService.hdel(topCacheKey, *secondCacheKeys.toTypedArray())
    }

    override fun add(topKey: String, secondKey: String): T {
        val obj = getValue(topKey, secondKey)
        add(topKey, secondKey, obj)
        return obj
    }

    override fun add(topKey: String, secondKey: String, obj: T) {
        val topCacheKey = appendKey(topKey)
        val nullValue: String? = redisService.hget(topCacheKey, NULL_PLACEHOLDER)
        if (obj == null) {
            if (nullValue == null) {
                redisService.hset(topCacheKey, NULL_PLACEHOLDER, NULL_PLACEHOLDER)
            }
            return
        }
        val secondCacheKey = appendSecondKey(secondKey)
        redisService.hset(topCacheKey, secondCacheKey, serialize(obj))
        redisService.expire(topCacheKey, expireSeconds)
        redisService.hdel(topCacheKey, NULL_PLACEHOLDER)
    }

    override fun saveAll(topKey: String): Map<String, T> {
        val objs = getAll(topKey)
        val mapToCache: Map<String, T> = objs.stream().collect(Collectors.toMap({ obj: T -> getSecondKey(obj) }, Function.identity()))
        saveAll(topKey, mapToCache)
        return mapToCache
    }

    override fun saveAll(topKey: String, mapToCache: Map<String, T>) {
        val topCacheKey = appendKey(topKey)
        redisService.del(topCacheKey)
        if (mapToCache.isEmpty()) {
            redisService.hset(topCacheKey, NULL_PLACEHOLDER, NULL_PLACEHOLDER)
            redisService.expire(topCacheKey, expireSeconds)
            return
        }
        val cache: MutableMap<String, String> = HashMap(mapToCache.size)
        mapToCache.forEach { (k: String, v: T) ->
            val secondCacheKey = appendSecondKey(k)
            cache[secondCacheKey] = serialize(v)
        }
        redisService.hmset(topCacheKey, cache)
        redisService.expire(topCacheKey, expireSeconds)
    }

    protected abstract fun serialize(obj: T): String
    protected abstract fun deserialize(value: String?): T
    protected abstract fun getValue(topKey: String?, secondKey: String?): T
    protected abstract fun appendSecondKey(key: String?): String
    protected abstract fun getSecondKey(obj: T): String?
    protected abstract fun getAll(key: String?): List<T>
}