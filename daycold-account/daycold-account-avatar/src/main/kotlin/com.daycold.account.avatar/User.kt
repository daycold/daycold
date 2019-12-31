package com.daycold.account.avatar

import java.time.LocalDate

/**
 * @author Stefan Liu
 */
interface User {
    val mobilePhone: String
    val userId: Long
}

interface Avatar : User {
    val id: Long
    val name: String
    val gender: String
    val birthDate: LocalDate
}

fun doA() {
    val a = 1
    val c = a::class
}
//
data class Customer(
    override val id: Long,
    override val name: String,
    override val userId: Long,
    override val gender: String,
    override val mobilePhone: String,
    override val birthDate: LocalDate
) : Avatar

data class Business(
    override val id: Long,
    override val name: String,
    override val userId: Long,
    override val gender: String,
    override val mobilePhone: String,
    override val birthDate: LocalDate
) : Avatar
