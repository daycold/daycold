package com.daycold.framework.redis.common

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import redis.clients.jedis.JedisPool
import java.io.Closeable
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import kotlin.jvm.Throws

/**
 * @author : liuwang
 * @date : 2020-08-20T17:54:41Z
 */
class RedisServiceInvocationHandler(
        private val jedisPool: JedisPool,
        private val log: Logger = LoggerFactory.getLogger(RedisService::class.java)
) : InvocationHandler {
    @Throws(Throwable::class)
    override fun invoke(proxy: Any, method: Method, args: Array<Any?>?): Any {
        if (method.declaringClass == Any::class.java) {
            return if (args == null) method.invoke(this) else method.invoke(this, *args)
        }
        if (jedisPool.isClosed) {
            throw UnsupportedOperationException("jedis has been closed")
        }
        if (method.declaringClass == Closeable::class.java || method.declaringClass == AutoCloseable::class.java) {
            return jedisPool.close()
        }
        jedisPool.resource.use { jedis ->
            val mills = System.currentTimeMillis()
            val result = if (args == null) method.invoke(jedis) else method.invoke(jedis, args)
            log.debug(buildLog(method, mills))
            return result
        }
    }

    private fun buildLog(method: Method, mills: Long): String {
        return StringBuilder().append(method.name)
                .append(" executes spending ")
                .append(System.currentTimeMillis() - mills)
                .append(" ms")
                .toString()
    }

    private fun getDefaultValue(method: Method): Any? {
        val type = method.returnType
        return when (type.name) {
            "int" -> 0
            "double" -> 0.0
            "float" -> 0f
            "long" -> 0L
            "byte" -> 0.toByte()
            "char" -> 0.toChar()
            "boolean" -> false
            else -> null
        }
    }
}