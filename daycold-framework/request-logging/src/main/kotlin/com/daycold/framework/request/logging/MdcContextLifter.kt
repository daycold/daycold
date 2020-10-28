package com.daycold.framework.request.logging

import org.slf4j.MDC
import reactor.core.CoreSubscriber
import reactor.util.context.Context
import java.util.stream.Collectors

/**
 * @author : liuwang
 * @date : 2020-10-28T14:03:11Z
 */
class MdcContextLifter<T>(private val delegate: CoreSubscriber<T>) : CoreSubscriber<T> by delegate {
    override fun onNext(t: T) {
        delegate.currentContext().copyToMdc()
        delegate.onNext(t)
    }

    private fun Context.copyToMdc() {
        if (isEmpty) {
            MDC.clear()
        } else {
            MDC.setContextMap(stream().collect(Collectors.toMap({ it.key.toString() }, { it.value.toString() })))
        }
    }
}