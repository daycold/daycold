package com.daycold.account.client.service

import com.daycold.account.client.model.request.AuthLoginRequest
import com.daycold.account.client.model.response.TokenResponse

/**
 * @author Stefan Liu
 */
interface AuthApiService {
    fun loginIn(authLogin: AuthLoginRequest): TokenResponse

    fun resetPassword(authLogin: AuthLoginRequest)

    fun loginUp(authLogin: AuthLoginRequest): TokenResponse
}