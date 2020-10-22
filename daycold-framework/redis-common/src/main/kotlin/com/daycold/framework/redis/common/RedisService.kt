package com.daycold.framework.redis.common

import redis.clients.jedis.commands.JedisCommands
import redis.clients.jedis.commands.MultiKeyCommands
import java.io.Closeable

/**
 * @author : liuwang
 * @date : 2020-08-20T17:51:09Z
 */
interface RedisService : JedisCommands, MultiKeyCommands, AutoCloseable, Closeable {
    companion object {
        const val SET_IF_ABSENT = "NX"
        const val SET_IF_PRESENT = "XX"
        const val EXPIRE_IN_SECONDS = "EX"
        const val EXPIRE_IN_MILLS = "PX"
        const val SET_SUCCESSFULLY = "OK"
    }
}