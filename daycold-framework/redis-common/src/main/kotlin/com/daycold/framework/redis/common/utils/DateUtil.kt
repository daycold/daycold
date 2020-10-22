package com.daycold.framework.redis.common.utils

/**
 * @author : liuwang
 * @date : 2020-09-24T14:11:04Z
 */
object DateUtil {
    const val SECONDS_PER_MINUTE = 60
    const val SECONDS_PER_HOUR = SECONDS_PER_MINUTE * 60
    const val SECONDS_PER_DAY = SECONDS_PER_HOUR * 24
    const val MILLS_PER_SECOND = 1000
    const val MILLS_PER_MINUTE = SECONDS_PER_MINUTE * MILLS_PER_SECOND.toLong()
    const val MILLS_PER_HOUR = SECONDS_PER_HOUR * MILLS_PER_SECOND.toLong()
    const val MILLS_PER_DAY = SECONDS_PER_DAY * MILLS_PER_SECOND.toLong()
}