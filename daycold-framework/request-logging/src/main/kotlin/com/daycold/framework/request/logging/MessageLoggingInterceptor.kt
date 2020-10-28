package com.daycold.framework.request.logging

import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageBuilder

/**
 * @author : liuwang
 * @date : 2020-10-28T12:01:45Z
 */
class MessageLoggingInterceptor : ChannelInterceptor {
    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
        val requestId = MDC.get(LoggingConfiguration.REQUEST_ID)
        if (requestId == null) {
            val requestIdHeader = message.headers[LoggingConfiguration.REQUEST_ID_HEADER]?.toString()
                    ?: LoggingConfiguration.generateRequestId()
            MDC.put(LoggingConfiguration.REQUEST_ID, requestIdHeader)
            log.info("收到 mq 消息: ${message.payload}")
            return MessageBuilder.fromMessage(message).setHeader(MDC_SET_KET, "1").build()
        } else {
            log.info("发送 mq 消息: ${message.payload}")
            return MessageBuilder.fromMessage(message).setHeader(LoggingConfiguration.REQUEST_ID_HEADER, requestId).build()
        }
    }

    override fun afterSendCompletion(message: Message<*>, channel: MessageChannel, sent: Boolean, ex: Exception?) {
        if (message.headers.containsKey(MDC_SET_KET)) {
            MDC.remove(LoggingConfiguration.REQUEST_ID)
        }
    }

    companion object {
        private const val MDC_SET_KET = "mdc";
        private val log = LoggerFactory.getLogger(MessageLoggingInterceptor::class.java)
    }
}