package com.daycold.framework.database.mybatis

/**
 * @author Stefan Liu
 */
annotation class MybatisDatabase(
    val name: String,
    val mapperPackage: Array<String>,
    val mapperXmlLocations: Array<String> = []
)