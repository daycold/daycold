package com.daycold.framework.redis.lettuce

import io.lettuce.core.RedisFuture
import kotlinx.coroutines.future.await
import java.util.concurrent.CompletionStage

class LettuceConfiguration {
}

suspend fun <V> RedisFuture<V>.awaitSuspend(): V = (this as CompletionStage<V>).await()

