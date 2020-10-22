package com.daycold.framework.redis.common.cache

/**
 * @author : liuwang
 * @date : 2020-09-23T10:06:04Z
 */
interface ZsetCacheService {
    fun save(key: String, value: String, score: Number)
    fun save(key: String): Map<String, Double>?
    fun delete(key: String): Long
    fun remove(key: String, value: String): Boolean
    fun range(key: String, start: Number, end: Number): Set<String>
    fun rangeAllOrCreate(key: String): Set<String>
    fun rangeAllOrCreate(key: String, start: Number, stop: Number): Set<String>
}