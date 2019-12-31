package com.daycold.framework.database.config

import com.zaxxer.hikari.HikariDataSource
import org.springframework.jdbc.core.JdbcTemplate
import javax.sql.DataSource

/**
 * @author Stefan Liu
 */
interface BaseDbFactory {
    fun createDataSource(): DataSource

    fun createJdbcTemplate(dataSource: DataSource): JdbcTemplate
}

abstract class AbstractDbFactory(
    private val databaseUrl: String,
    private val databaseUsername: String,
    private val databasePassword: String,
    private val poolSize: Int
) : BaseDbFactory {

    override fun createJdbcTemplate(dataSource: DataSource) = JdbcTemplate(dataSource, true)

    protected fun doCreateDataSource(databaseName: String) = HikariDataSource().apply {
        poolName = "hikari-pool-" + databaseName.toLowerCase()
        driverClassName = "com.mysql.jdbc.Driver"
        jdbcUrl = databaseUrl
        username = databaseUsername
        password = databasePassword
        maximumPoolSize = poolSize
        minimumIdle = DEFAULT_MIN_POOL_SIZE
        connectionTimeout = DEFAULT_CONNECT_TIMEOUT
        validationTimeout = DEFAULT_VALIDATION_TIMEOUT
        idleTimeout = MAX_IDLE_TIME
        connectionInitSql = null
        initializationFailTimeout = 0
    }

    companion object {
        const val DEFAULT_MIN_POOL_SIZE = 1
        const val DEFAULT_CONNECT_TIMEOUT = 3L * 1000
        const val DEFAULT_VALIDATION_TIMEOUT = 5L * 1000
        const val MAX_IDLE_TIME = 1L * 60 * 1000
    }
}