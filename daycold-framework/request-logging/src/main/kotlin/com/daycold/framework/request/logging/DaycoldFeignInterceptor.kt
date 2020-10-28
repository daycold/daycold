package com.daycold.framework.request.logging

import feign.RequestInterceptor
import feign.RequestTemplate
import org.slf4j.MDC

/**
 * @author : liuwang
 * @date : 2020-10-28T11:22:22Z
 */
class DaycoldFeignInterceptor : RequestInterceptor {
    override fun apply(template: RequestTemplate) {
        val requestId = MDC.get(LoggingConfiguration.REQUEST_ID)
        if (requestId != null) template.header(LoggingConfiguration.REQUEST_ID_HEADER, requestId)
    }
}