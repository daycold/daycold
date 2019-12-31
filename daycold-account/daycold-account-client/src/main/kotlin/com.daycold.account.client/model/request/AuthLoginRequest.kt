package com.daycold.account.client.model.request

/**
 * @author Stefan Liu
 */
data class AuthLoginRequest(
    val mobilePhone: String,
    val password: String? = null,
    val smsCode: String? = null
)