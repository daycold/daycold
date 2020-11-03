package com.daycold.framework.feign.test

import feign.Feign
import feign.InvocationHandlerFactory
import feign.Retryer
import feign.Target
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
//import org.springframework.cloud.netflix.feign.FeignClientsConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

/**
 * @author : liuwang
 * @date : 2020-11-03T11:02:39Z
 */
@Configuration
//@AutoConfigureBefore(FeignClientsConfiguration::class)
@Deprecated("feign.client.config could set loggerLevel and log execution time")
class DcFeignConfiguration {
    @Bean
    @ConditionalOnMissingBean
    fun feignRetryer(): Retryer {
        return Retryer.NEVER_RETRY;
    }

    @Bean
    @Scope("prototype")
    @ConditionalOnMissingBean
    fun feignBuilder(retryer: Retryer): Feign.Builder {
        return Feign.builder().invocationHandlerFactory(DcFeignInvocationHandlerFactory()).retryer(retryer)
    }
}

class DcFeignInvocationHandler(target: Target<*>, private val dispatch: Map<Method, InvocationHandlerFactory.MethodHandler>) : InvocationHandler {
    private val log = LoggerFactory.getLogger(target.type())

    override fun invoke(proxy: Any?, method: Method, args: Array<out Any>?): Any {
        if (method.declaringClass == Any::class.java) {
            return if (args == null) method.invoke(this) else method.invoke(this, args)
        }
        val mills = System.currentTimeMillis()
        try {
            return (dispatch[method] ?: error("invalid feign handler method")).invoke(args)
        } finally {
            log.info("{} executes {} ms", method.name, System.currentTimeMillis() - mills)
        }
    }
}

class DcFeignInvocationHandlerFactory : InvocationHandlerFactory {
    override fun create(target: Target<*>, dispatch: MutableMap<Method, InvocationHandlerFactory.MethodHandler>): InvocationHandler {
        return DcFeignInvocationHandler(target, dispatch)
    }
}