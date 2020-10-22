package com.daycold.framework.redis.common

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
import org.springframework.boot.autoconfigure.data.redis.RedisProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnection
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.data.redis.core.RedisConnectionUtils
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import java.io.Closeable
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

/**
 * @author : liuwang
 * @date : 2020-08-20T17:55:13Z
 */
@Configuration
@ConditionalOnClass(JedisPool::class)
class RedisConfiguration {
    @Configuration
    @ConditionalOnBean(RedisConnectionFactory::class)
    @AutoConfigureAfter(RedisAutoConfiguration::class)
    class Demo {
        @Bean
        fun getRedisService(jedisConnectionFactory: JedisConnectionFactory): RedisService {
            return Proxy.newProxyInstance(RedisService::class.java.classLoader, arrayOf<Class<*>>(RedisService::class.java),
                    RedisServiceDataInvocator(jedisConnectionFactory)) as RedisService
        }
    }

    @Configuration
    @ConditionalOnMissingBean(RedisConnectionFactory::class)
    class Test(private val properties: RedisProperties) {
        @Bean
        fun getRedisService(): RedisService {
            return Proxy.newProxyInstance(RedisService::class.java.classLoader, arrayOf<Class<*>>(RedisService::class.java),
                    RedisServiceInvocator(createJedisPool())) as RedisService
        }

        private fun createJedisPool(): JedisPool {
            return JedisPool(JedisPoolConfig(), properties.host, properties.port,
                    properties.timeout?.toMillis()?.toInt() ?: 5000,
                    properties.password, properties.database, "default")
        }
    }

    class RedisServiceDataInvocator(
            private val jedisConnectionFactory: JedisConnectionFactory,
            private val log: Logger = LoggerFactory.getLogger(RedisService::class.java)
    ) : InvocationHandler {
        override fun invoke(proxy: Any?, method: Method, args: Array<out Any?>?): Any {
            if (method.declaringClass == Any::class.java) {
                return if (args == null) method.invoke(this) else method.invoke(this, *args)
            }
            if (jedisConnectionFactory.)
                if (method.declaringClass == Closeable::class.java || method.declaringClass == AutoCloseable::class.java) {
                    return jedisConnectionFactory.destroy()
                }
            var connection: RedisConnection? = null
            try {
                val mills = System.currentTimeMillis()
                connection = jedisConnectionFactory.connection
                val result = if (args == null) method.invoke(connection) else method.invoke(connection, *args)
                log.debug(buildLog(method, mills))
                return result
            } catch (e: Exception) {
                throw UnsupportedOperationException("jedis has been closed")
            } finally {
                RedisConnectionUtils.releaseConnection(connection, jedisConnectionFactory, false)
            }
        }
    }


    class RedisServiceInvocator(
            private val jedisPool: JedisPool,
            private val log: Logger = LoggerFactory.getLogger(RedisService::class.java)
    ) : InvocationHandler {
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
    }

    companion object {
        private fun getDefaultValue(method: Method) = when (method.returnType.name) {
            "int" -> 0
            "double" -> 0.0
            "float" -> 0f
            "long" -> 0L
            "byte" -> 0.toByte()
            "char" -> 0.toChar()
            "boolean" -> false
            else -> null
        }

        private fun buildLog(method: Method, mills: Long): String {
            return StringBuilder().append(method.name)
                    .append(" executes spending ")
                    .append(System.currentTimeMillis() - mills)
                    .append(" ms")
                    .toString()
        }
    }
}
