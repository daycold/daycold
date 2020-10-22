package com.daycold.framework.redis.common.lock

/**
 * @author : liuwang
 * @date : 2020-08-21T18:12:30Z
 */
interface RedisLock {
    fun tryLock(): Boolean
    fun unlock()
}