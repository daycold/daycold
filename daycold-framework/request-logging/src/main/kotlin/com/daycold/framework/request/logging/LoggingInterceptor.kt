package com.daycold.framework.request.logging

import org.slf4j.MDC
import org.springframework.web.servlet.HandlerInterceptor
import java.lang.Exception
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @author : liuwang
 * @date : 2020-10-28T11:52:30Z
 */
class LoggingInterceptor : HandlerInterceptor {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        initRequestId(request)
        return true
    }

    override fun afterCompletion(request: HttpServletRequest, response: HttpServletResponse, handler: Any, ex: Exception?) {
        clearRequestId()
    }
    private fun clearRequestId() {
        MDC.clear()
    }

    private fun initRequestId(request: HttpServletRequest) {
        val requestId = request.getHeader(LoggingConfiguration.REQUEST_ID_HEADER) ?: LoggingConfiguration.generateRequestId()
        MDC.put(LoggingConfiguration.REQUEST_ID, requestId)
    }
}