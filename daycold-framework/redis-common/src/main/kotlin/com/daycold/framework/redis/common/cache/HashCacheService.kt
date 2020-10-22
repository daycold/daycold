package com.daycold.framework.redis.common.cache

/**
 * @author : liuwang
 * @date : 2020-08-20T18:10:33Z
 */
interface HashCacheService<T> {
    fun findAll(key: String): Map<String, T?>
    fun findOrCreateAll(key: String): Map<String, T?>
    fun find(topKey: String, secondKey: String): T?
    fun findOrCreate(topKey: String, secondKey: String): T?
    fun delete(topKeys: Collection<String>): Long
    fun delete(topKey: String): Long
    fun remove(topKey: String, secondKeys: Collection<String>): Long
    fun add(topKey: String, secondKey: String): T
    fun add(topKey: String, secondKey: String, obj: T)
    fun saveAll(topKey: String): Map<String, T>
    fun saveAll(topKey: String, mapToCache: Map<String, T>)
    fun clearAll()
}