package com.daycold.framework.redis.common.cache

import com.daycold.framework.redis.common.RedisService
import com.daycold.framework.redis.common.lock.RedisLock
import com.daycold.framework.redis.common.lock.RedisReentrantLock
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import redis.clients.jedis.ScanParams
import redis.clients.jedis.ScanResult

/**
 * @author : liuwang
 * @date : 2020-08-21T17:47:48Z
 */
abstract class AbstractCacheService : ApplicationContextAware, InitializingBean {
    @Value("spring.application.name")
    private var applicationName: String? = null
    private var applicationContext: ApplicationContext? = null

    private var redisServiceField: RedisService? = null

    protected val redisService: RedisService
        get() = redisServiceField!!


    override fun setApplicationContext(applicationContext: ApplicationContext?) {
        this.applicationContext = applicationContext
    }

    override fun afterPropertiesSet() {
        redisServiceField = applicationContext!!.getBean(RedisService::class.java)
    }

    fun clearAll() {
        val lock = getRedisLock(keyPatternPrefix)
        if (lock.tryLock()) {
            try {
                val scanParams: ScanParams = ScanParams().count(500).match(keyPatternForClearing)
                var cursor = "0"
                while (true) {
                    val result: ScanResult<String> = redisService.scan(cursor, scanParams)
                    cursor = result.cursor
                    if (!result.getResult().isEmpty()) {
                        redisService.del(*result.result.toTypedArray())
                    }
                    if ("0" == result.cursor) {
                        break
                    }
                }
            } finally {
                lock.unlock()
            }
        }
    }

    fun delete(key: String): Long {
        val cacheKey = appendKey(key)
        return redisService.del(cacheKey)
    }

    fun existed(key: String): Boolean {
        return redisService.exists(appendKey(key))
    }

    protected val keyPatternForClearing: String
        protected get() = "$keyPatternPrefix*"

    protected fun appendKey(key: String): String {
        return keyPatternPrefix + key
    }

    protected abstract val keyPatternPrefix: String
    protected fun getRedisLock(key: String): RedisLock {
        return RedisReentrantLock(redisService!!, buildKey(key))
    }

    protected fun getRedisLock(key: String, expireMills: Long): RedisLock {
        return RedisReentrantLock(redisService!!, buildKey(key), expireMills)
    }

    private fun buildKey(key: String): String {
        return StringBuilder(applicationName).append(':').append(key).toString()
    }

    companion object {
        const val NULL_PLACEHOLDER = "{}"
    }
}