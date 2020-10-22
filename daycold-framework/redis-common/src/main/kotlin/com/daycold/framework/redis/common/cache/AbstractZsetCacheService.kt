package com.daycold.framework.redis.common.cache

import com.daycold.framework.redis.common.utils.DateUtil
import java.util.*

/**
 * @param <T>
 * @author : liuwang
 * @date : 2020-09-23T11:53:20Z
</T> */
abstract class AbstractZsetCacheService<T> protected constructor(
        private val expireSeconds: Int = DateUtil.SECONDS_PER_DAY
) : AbstractCacheService(), ZsetCacheService {
    override fun save(key: String, value: String, score: Number) {
        val cacheKey = appendKey(key)
        val existed: Boolean = redisService.exists(cacheKey)
        if (!existed) {
            save(key)
        }
        redisService.zadd(cacheKey, score.toDouble(), value)
    }

    override fun save(key: String): Map<String, Double> {
        val results = getValues(key)
        val cacheKey = appendKey(key)
        if (redisService.exists(cacheKey)) {
            redisService.del(cacheKey)
        }
        val scores: MutableMap<String, Double> = if (results.isEmpty()) emptyMap else getScores(results)
        redisService.zadd(cacheKey, scores)
        redisService.expire(cacheKey, expireSeconds)
        scores.remove(NULL_PLACEHOLDER)
        return scores
    }

    override fun remove(key: String, value: String): Boolean {
        return redisService.zrem(appendKey(key), value) == 1L
    }

    override fun range(key: String, start: Number, end: Number): Set<String> {
        val result: MutableSet<String> = redisService.zrange(appendKey(key), start.toLong(), end.toLong())
        result.remove(NULL_PLACEHOLDER)
        return result
    }

    override fun rangeAllOrCreate(key: String): Set<String> {
        val cacheKey = appendKey(key)
        val result: MutableSet<String> = redisService.zrange(cacheKey, 0, -1)
        if (!result.isEmpty()) {
            result.remove(NULL_PLACEHOLDER)
            return result
        }
        return save(key).keys
    }

    override fun rangeAllOrCreate(key: String, start: Number, stop: Number): Set<String> {
        val cacheKey = appendKey(key)
        if (!redisService.exists(cacheKey)) {
            save(key)
        }
        return range(key, start, stop)
    }

    protected abstract fun getScores(objs: List<T>): MutableMap<String, Double>
    protected abstract fun getValues(key: String): List<T>
    protected val emptyMap: MutableMap<String, Double>
        protected get() {
            val result: MutableMap<String, Double> = HashMap(1)
            result[NULL_PLACEHOLDER] = 1.0
            return result
        }
}