package com.daycold.framework.caffeine

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

/**
 * @author : liuwang
 * @date : 2020-10-23T09:29:21Z
 */
annotation class CacheBatch(
        val expireSeconds: Long = 60
)

@Aspect
open class CacheBatchAspect {
    private val itemCache = ConcurrentHashMap<String, CacheBatchAspectItem>()
    private val cacheMap = ConcurrentHashMap<Long, Cache<String, Any>>()

    @Around("@annotation(com.daycold.framework.caffeine.CacheBatch) && @annotation(cacheBatch)")
    open fun handleCacheBatch(joinPoint: ProceedingJoinPoint, cacheBatch: CacheBatch): Any? {
        val aspectItem = getAspectItem(joinPoint)
        val args = joinPoint.args
        val keyMap = getCacheKey(aspectItem, args)
        if (keyMap.isEmpty()) return emptyMap<Any, Any>()
        val cacheResult = getCache(cacheBatch).getAll(keyMap.keys) { keys ->
            val collection = keyMap.entries.filter { keys.contains(it.key) }.map { it.value }
            handleArguments(aspectItem, args, collection)
            val out = joinPoint.proceed(args) as? Map<Any, Any?> ?: emptyMap()
            keys.associateWith { out.getOrDefault(keyMap.getValue(it), None) }
        }
        return keyMap.entries.associate { it.value to handCacheValue(cacheResult.getValue(it.key)) }
    }

    private fun handCacheValue(obj: Any): Any? {
        if (obj === None) {
            return null;
        }
        return obj;
    }

    private fun handleArguments(aspectItem: CacheBatchAspectItem, originArgs: Array<Any?>, items: Collection<Any?>) {
        val index = aspectItem.index
        originArgs[index] = items
    }

    private fun getCache(cacheBatch: CacheBatch): Cache<String, Any> {
        return cacheMap.computeIfAbsent(cacheBatch.expireSeconds) {
            Caffeine.newBuilder().expireAfterWrite(Duration.ofSeconds(it)).build()
        }
    }

    private fun getCacheKey(aspectItem: CacheBatchAspectItem, args: Array<Any?>): Map<String, Any> {
        val index = aspectItem.index
        val prefix = aspectItem.id
        val collection = args[index] as? Collection<Any?> ?: return emptyMap()
        return collection.filterNotNull().associateBy {
            val builder = StringBuilder(prefix).append(':')
            for (i in args.indices) {
                builder.append(if (i == index) it else args[i]).append(':')
            }
            builder.toString()
        }
    }

    private fun getAspectItem(joinPoint: ProceedingJoinPoint): CacheBatchAspectItem {
        val name = joinPoint.toLongString()
        return itemCache.computeIfAbsent(name) { buildAspectItem(joinPoint) }
    }

    private fun buildAspectItem(joinPoint: ProceedingJoinPoint): CacheBatchAspectItem {
        val methodSignature = joinPoint.signature as MethodSignature
        val returnType = methodSignature.returnType
        var index = -1
        val types = methodSignature.parameterTypes
        if (returnType != Map::class.java) throw IllegalArgumentException("返回值必须申明为 Map， 且 key 与参数中集合类的元素类型一致")
        types.withIndex().forEach {
            val i = it.index
            val type = it.value
            if (Map::class.java.isAssignableFrom(type)) throw IllegalArgumentException("参数不能使用 Map ")
            if (Collection::class.java.isAssignableFrom(type)) {
                if (type != Collection::class.java) throw IllegalArgumentException("请使用 Collection 类申明集合参数类型")
                if (index >= 0) throw IllegalArgumentException("参数不能使用多个集合类")
                index = i
            }
        }
        if (index < 0) throw IllegalArgumentException("必须存在一个 Collection 参数")
        return CacheBatchAspectItem(methodSignature.method.name, index)
    }

    companion object None : Any()
}


private data class CacheBatchAspectItem(
        val id: String,
        val index: Int
)
