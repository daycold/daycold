package com.daycold.framework.jpa.mongo

import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import org.slf4j.LoggerFactory
import org.springframework.aop.framework.ProxyFactory
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.config.MongoRepositoryConfigurationExtension
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactoryBean
import org.springframework.data.repository.Repository
import org.springframework.data.repository.config.DefaultRepositoryBaseClass
import org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport
import org.springframework.data.repository.config.RepositoryConfigurationExtension
import org.springframework.data.repository.core.RepositoryInformation
import org.springframework.data.repository.core.support.RepositoryFactorySupport
import org.springframework.data.repository.core.support.RepositoryProxyPostProcessor
import org.springframework.data.repository.query.QueryLookupStrategy
import java.io.Serializable
import java.lang.annotation.Inherited
import kotlin.reflect.KClass

/**
 * @author : liuwang
 * @date : 2020-11-03T10:34:43Z
 */
@Document
@Inherited
@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
@Import(DcMongoRepositoriesRegistrar::class)
annotation class EnableDcMongoRepositories(
        val value: Array<String> = [],
        val basePackages: Array<String> = [],
        val basePackageClasses: Array<KClass<*>> = [],
        val includeFilters: Array<ComponentScan.Filter> = [],
        val excludeFilters: Array<ComponentScan.Filter> = [],
        val repositoryImplementationPostfix: String = "Impl",
        val namedQueriesLocation: String = "",
        val queryLookupStrategy: QueryLookupStrategy.Key = QueryLookupStrategy.Key.CREATE_IF_NOT_FOUND,
        val repositoryFactoryBeanClass: KClass<*> = DcMongoRepositoryFactoryBean::class,
        val repositoryBaseClass: KClass<*> = DefaultRepositoryBaseClass::class,
        val mongoTemplateRef: String = "mongoTemplate",
        val createIndexesForQueryMethods: Boolean = false,
        val considerNestedRepositories: Boolean = false
)

class DcMongoRepositoriesRegistrar : RepositoryBeanDefinitionRegistrarSupport() {
    override fun getExtension(): RepositoryConfigurationExtension {
        return DcRepositoryConfigurationExtension()
    }

    override fun getAnnotation(): Class<out Annotation> {
        return EnableDcMongoRepositories::class.java
    }
}

class DcRepositoryConfigurationExtension : MongoRepositoryConfigurationExtension() {
    override fun getRepositoryFactoryBeanClassName(): String {
        return DcMongoRepositoryFactoryBean::class.java.name
    }
}

class DcMongoRepositoryFactoryBean<T : Repository<S, ID>, S, ID : Serializable>(
        repositoryInterface: Class<out T>
) : MongoRepositoryFactoryBean<T, S, ID>(repositoryInterface) {
    override fun getFactoryInstance(operations: MongoOperations): RepositoryFactorySupport {
        return super.getFactoryInstance(operations).apply {
            addRepositoryProxyPostProcessor(DcRepositoryProxyPostProcessor())
        }
    }
}

class DcRepositoryProxyPostProcessor : RepositoryProxyPostProcessor {
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