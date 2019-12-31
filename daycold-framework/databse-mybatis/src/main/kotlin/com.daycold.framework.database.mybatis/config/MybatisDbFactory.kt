package com.daycold.framework.database.mybatis.config

import com.daycold.framework.database.config.AbstractDbFactory

/**
 * @author Stefan Liu
 */
class MybatisDbFactory(
    databaseUrl: String, databaseUsername: String,
    databasePassword: String, poolSize: Int
) : AbstractDbFactory(databaseUrl, databaseUsername, databasePassword, poolSize) {
    override fun createDataSource() = doCreateDataSource("")
}