package com.daycold.account.client.model.request

/**
 * @author Stefan Liu
 */
data class SmsAlarmRequest(
    val mobilePhone: String,
    val message: String
)