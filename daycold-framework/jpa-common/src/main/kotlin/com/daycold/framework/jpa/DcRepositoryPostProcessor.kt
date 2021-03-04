package com.daycold.framework.jpa

import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import org.slf4j.LoggerFactory
import org.springframework.aop.framework.ProxyFactory
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.annotation.Configuration
import org.springframework.data.repository.core.RepositoryInformation
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport
import org.springframework.data.repository.core.support.RepositoryFactorySupport
import org.springframework.data.repository.core.support.RepositoryProxyPostProcessor

@Configuration
class DcRepositoryPostProcessor : BeanPostProcessor {
    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any? {
        if (bean is RepositoryFactoryBeanSupport<*, *, *>) {
            bean.setLazyInit(true)
        }
        return bean
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        if (bean is RepositoryFactoryBeanSupport<*, *, *>) {
            val support = getRepositoryFactorySupport(bean)
            support.addRepositoryProxyPostProcessor(DcRepositoryProcessor())
        }
        return bean
    }

    private fun getRepositoryFactorySupport(beanSupport: RepositoryFactoryBeanSupport<*, *, *>): RepositoryFactorySupport {
        val clazz = beanSupport.javaClass
        val field = clazz.getField("factory")
        field.isAccessible = true
        val factory = field.get(beanSupport) as RepositoryFactorySupport
        field.isAccessible = false
        return factory
    }
}

class DcRepositoryProcessor : RepositoryProxyPostProcessor {
    override fun postProcess(factory: ProxyFactory, repositoryInformation: RepositoryInformation) {
        factory.addAdvice(DcRepositoryAdvice(repositoryInformation.repositoryInterface.name))
    }
}

class DcRepositoryAdvice(loggerName: String) : MethodInterceptor {
    private val log = LoggerFactory.getLogger(loggerName)

    override fun invoke(invocation: MethodInvocation): Any {
        val mills = System.currentTimeMillis()
        val result = invocation.proceed()
        log.info("{} executes {} ms", invocation.method.name, System.currentTimeMillis() - mills)
        return result;
    }
}

