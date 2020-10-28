package com.daycold.framework.request.logging

import feign.RequestInterceptor
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import reactor.core.publisher.Hooks
import reactor.core.publisher.Mono
import reactor.core.publisher.Operators
import java.util.UUID
import java.util.concurrent.ExecutorService

/**
 * @author : liuwang
 * @date : 2020-10-28T11:23:42Z
 */
@Configuration
class LoggingConfiguration : BeanPostProcessor {
    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        if (bean is ExecutorService) {
            return ExecutorServiceLoggingWrapper(bean)
        }
        return bean
    }

    @Configuration
    @ConditionalOnClass(WebMvcConfigurer::class)
    class LoggingMvcConfigurer : WebMvcConfigurer {
        override fun addInterceptors(registry: InterceptorRegistry) {
            registry.addInterceptor(LoggingInterceptor())
        }
    }

    @Configuration
    @ConditionalOnClass(WebFluxConfigurer::class)
    class LoggingReactorConfigurer : WebFilter, InitializingBean {
        override fun afterPropertiesSet() {
            Hooks.onEachOperator(LIFTER_NAME, Operators.lift { _, subscriber -> MdcContextLifter(subscriber) })
            Hooks.resetOnEachOperator(LIFTER_NAME)
        }

        override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
            val requestId = exchange.request.headers[REQUEST_ID_HEADER]?.firstOrNull() ?: generateRequestId()
            return chain.filter(exchange).subscriberContext { it.put(REQUEST_ID, requestId) }
        }
    }

    @Configuration
    @ConditionalOnClass(RequestInterceptor::class)
    class FeignConfiguration {
        @Bean
        fun feignInterceptor(): RequestInterceptor {
            return DaycoldFeignInterceptor()
        }
    }

    @Configuration
    @ConditionalOnBean(MessageChannel::class)
    class MessageConfiguration {
        @Bean
        fun messageLoggingInterceptor(): ChannelInterceptor {
            return MessageLoggingInterceptor()
        }
    }

    @Configuration
    @ConditionalOnBean(org.springframework.integration.channel.AbstractMessageChannel::class)
    class IntegrationLoggingMessageConfiguration(
            private val interceptor: MessageLoggingInterceptor
    ) : BeanPostProcessor {
        override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
            if (bean is org.springframework.integration.channel.AbstractMessageChannel) {
                bean.addInterceptor(interceptor)
            }
            return bean;
        }
    }

    @Configuration
    @ConditionalOnBean(org.springframework.messaging.support.AbstractMessageChannel::class)
    class SupportLoggingMessageConfiguration(
            private val interceptor: MessageLoggingInterceptor
    ) : BeanPostProcessor {
        override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
            if (bean is org.springframework.messaging.support.AbstractMessageChannel) {
                bean.addInterceptor(interceptor)
            }
            return bean;
        }
    }

    companion object {
        const val REQUEST_ID = "requestId"
        const val REQUEST_ID_HEADER = "DC-REQUEST-ID"
        private val LIFTER_NAME = MdcContextLifter::class.java.name

        fun generateRequestId(): String {
            return UUID.randomUUID().toString()
        }
    }
}

