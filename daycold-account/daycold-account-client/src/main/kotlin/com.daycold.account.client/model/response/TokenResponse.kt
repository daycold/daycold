package com.daycold.account.client.model.response

import org.joda.time.DateTime

/**
 * @author Stefan Liu
 */
data class TokenResponse(val accessToken: String,
    val expireTime: DateTime)
