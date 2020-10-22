package com.daycold.framework.redis.common.cache

import com.daycold.framework.redis.common.utils.DateUtil
import java.util.*
import java.util.function.Consumer

/**
 * @author : liuwang
 * @date : 2020-08-20T17:55:53Z
 */
abstract class AbstractValueCacheService<T> protected constructor(
        private val expireSeconds: Int = DateUtil.SECONDS_PER_DAY
) : AbstractCacheService(), ValueCacheService<T> {
    override fun find(key: String): T? {
        val cacheKey = appendKey(key)
        val cachedValue: String? = redisService.get(cacheKey)
        return if (cachedValue == null || cachedValue == NULL_PLACEHOLDER) {
            null
        } else deserialize(cachedValue)
    }

    override fun findOrEmpty(key: String): T? {
        val cacheKey = appendKey(key)
        val cachedValue: String = redisService.get(cacheKey) ?: return null
        return deserialize(cachedValue)
    }

    override fun findOrCreate(key: String): T? {
        val cacheKey = appendKey(key)
        return when (val cachedValue: String? = redisService.get(cacheKey)) {
            null -> save(key)
            NULL_PLACEHOLDER -> null
            else -> deserialize(cachedValue)
        }
    }

    override fun findAll(keys: List<String>): MutableMap<String, T?> {
        val results: List<String?> = redisService.mget(*keys.map { key: String? -> appendKey(key!!) }.toTypedArray())
        val resultMap: MutableMap<String, T?> = HashMap(keys.size)
        for (i in keys.indices) {
            val key = keys[i]
            val value = results[i]
            if (value != null) {
                resultMap[key] = if (value == NULL_PLACEHOLDER) null else deserialize(value)
            }
        }
        return resultMap
    }

    override fun findAllOrEmpty(keys: List<String>): Map<String, T> {
        val results: List<String?> = redisService.mget(*keys.map { key: String -> appendKey(key) }.toTypedArray())
        val resultMap: MutableMap<String, T> = HashMap(keys.size)
        for (i in keys.indices) {
            val key = keys[i]
            val value = results[i]
            if (value != null) {
                resultMap[key] = deserialize(value)
            }
        }
        return resultMap
    }

    override fun findOrCreateAll(keys: List<String>): Map<String, T?> {
        val resultMap = findAll(keys)
        if (resultMap.size != keys.size) {
            val invalidKeys: MutableList<String> = keys.toMutableList()
            invalidKeys.removeAll(resultMap.keys)
            val newResults = getValues(invalidKeys)
            invalidKeys.removeAll(newResults.keys)
            invalidKeys.forEach(Consumer { key: String -> newResults[key] = null })
            newResults.forEach { (key: String, obj: T?) -> this.save(key, obj) }
            resultMap.putAll(newResults)
        }
        return resultMap
    }

    override fun delete(keys: Collection<String>): Long {
        if (keys.isEmpty()) {
            return 0L
        }
        val cacheKeys: Array<String> = keys.map { key: String -> appendKey(key) }.toTypedArray()
        return redisService.del(*cacheKeys)
    }

    override fun save(key: String): T {
        val obj = getValue(key)
        save(key, obj)
        return obj
    }

    override fun save(key: String, obj: T?) {
        val cacheKey = appendKey(key)
        val valueToCache = serialize(obj)
        redisService.setex(cacheKey, expireSeconds, valueToCache)
    }

    override fun isEmptyObject(obj: T?): Boolean {
        return obj == null || emptyObject === obj
    }

    private fun serialize(obj: T?): String {
        return if (isEmptyObject(obj)) {
            NULL_PLACEHOLDER
        } else doSerialize(obj!!)
    }

    private fun deserialize(value: String): T {
        return if (NULL_PLACEHOLDER == value) {
            emptyObject
        } else doDeserialize(value)
    }

    protected abstract val emptyObject: T
    protected abstract fun doSerialize(obj: T): String
    protected abstract fun doDeserialize(value: String?): T
    protected abstract fun getValue(key: String): T
    protected abstract fun getValues(keys: Collection<String>): MutableMap<String, T?>
}