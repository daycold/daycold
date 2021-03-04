package com.daycold.framework.redis.common.lock

import com.daycold.framework.redis.common.RedisService
import com.daycold.framework.redis.common.utils.DateUtil
import org.redisson.RedissonLock
import org.redisson.api.RLock
import redis.clients.jedis.params.SetParams

/**
 * @author : liuwang
 * @date : 2020-08-21T18:24:37Z
 */
class RedisReentrantLock constructor(
    private val redisService: RedisService,
    lockKey: String, expireMills: Long = DateUtil.MILLS_PER_MINUTE
) : RedisLock {
    private val expireMills: Long
    private val lockKey: String
    private var reentrantCount = 0
    private val value = generateValue()
    override fun tryLock(): Boolean {
        val cachedValue: String? = redisService.get(lockKey)
        if (cachedValue == null) {
            val result: String = redisService.set(lockKey, value, SetParams().px(expireMills).nx())
            if (RedisService.SET_SUCCESSFULLY == result) {
                reentrantCount++
                return true
            }
        } else if (cachedValue == value) {
            reentrantCount++
            return true
        }
        return false
    }

    override fun unlock() {
        reentrantCount--
        if (reentrantCount == 0) {
            redisService.del(lockKey)
        }
    }

    private fun generateValue(): String {
        return System.currentTimeMillis().toString() + Thread.currentThread().id
    }

    private fun buildLockKey(key: String): String {
        return StringBuilder(LOCK_KEY_PREFIX).append(':').append(key).toString()
    }

    companion object {
        private const val LOCK_KEY_PREFIX = "reentrant-lock"
    }

    init {
        this.lockKey = buildLockKey(lockKey)
        this.expireMills = expireMills
    }

    fun demo() {
        val lock: RLock = RedissonLock(null, "hello")
    }
}