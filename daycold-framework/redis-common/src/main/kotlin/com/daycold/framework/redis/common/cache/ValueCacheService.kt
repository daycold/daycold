package com.daycold.framework.redis.common.cache

/**
 * @author : liuwang
 * @date : 2020-08-20T17:35:38Z
 */
interface ValueCacheService<T> {
    /**
     * key 不存在或为空对象时返回 null
     */
    fun find(key: String): T?

    /**
     * key 不存在时返回 null，key 存在且为空时返回 空对象
     */
    fun findOrEmpty(key: String): T?

    /**
     * 成功时返回新对象或 null
     */
    fun findOrCreate(key: String): T?

    /**
     * value 为新对象或 null
     */
    fun findAll(keys: List<String>): Map<String, T?>

    /**
     * value 为新对象或空对象
     */
    fun findAllOrEmpty(keys: List<String>): Map<String, T>

    /**
     * value 为新对象或 null
     */
    fun findOrCreateAll(keys: List<String>): Map<String, T?>
    fun delete(keys: Collection<String>): Long
    fun delete(key: String): Long

    /**
     * value 为新对象或null
     */
    fun save(key: String): T?
    fun save(key: String, obj: T?)
    fun clearAll()

    /**
     * 空对象时 true
     */
    fun isEmptyObject(obj: T?): Boolean
}