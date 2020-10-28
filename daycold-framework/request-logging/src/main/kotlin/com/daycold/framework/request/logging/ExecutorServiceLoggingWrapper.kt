package com.daycold.framework.request.logging

import org.slf4j.MDC
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

/**
 * @author : liuwang
 * @date : 2020-10-28T11:37:58Z
 */
class ExecutorServiceLoggingWrapper(private val delegate: ExecutorService) : ExecutorService by delegate {
    override fun <T : Any?> submit(task: Callable<T>): Future<T> {
        return delegate.submit(LoggingCallable(task))
    }

    override fun submit(task: Runnable): Future<*> {
        return delegate.submit(LoggingRunnable(task))
    }

    override fun <T : Any?> invokeAny(tasks: MutableCollection<out Callable<T>>): T {
        return delegate.invokeAny(tasks.map { LoggingCallable(it) })
    }

    override fun <T : Any?> invokeAny(tasks: MutableCollection<out Callable<T>>, timeout: Long, unit: TimeUnit): T {
        return delegate.invokeAny(tasks.map { LoggingCallable(it) }, timeout, unit)
    }

    override fun <T : Any?> submit(task: Runnable, result: T): Future<T> {
        return delegate.submit(LoggingRunnable(task), result)
    }

    override fun <T : Any?> invokeAll(tasks: MutableCollection<out Callable<T>>): MutableList<Future<T>> {
        return delegate.invokeAll(tasks.map { LoggingCallable(it) })
    }

    override fun <T : Any?> invokeAll(tasks: MutableCollection<out Callable<T>>, timeout: Long, unit: TimeUnit): MutableList<Future<T>> {
        return delegate.invokeAll(tasks.map { LoggingCallable(it) }, timeout, unit)
    }

    override fun execute(command: Runnable) {
        delegate.execute(LoggingRunnable(command))
    }

    private class LoggingRunnable(private val task: Runnable) : Runnable {
        private val requestId = MDC.get(LoggingConfiguration.REQUEST_ID)

        override fun run() {
            try {
                if (requestId != null) MDC.put(LoggingConfiguration.REQUEST_ID, requestId)
                task.run()
            } finally {
                MDC.clear()
            }
        }
    }

    private class LoggingCallable<V>(private val task: Callable<V>) : Callable<V> {
        private val requestId = MDC.get(LoggingConfiguration.REQUEST_ID)

        override fun call(): V {
            try {
                if (requestId != null) MDC.put(LoggingConfiguration.REQUEST_ID, requestId)
                return task.call()
            } finally {
                MDC.clear()
            }
        }
    }
}