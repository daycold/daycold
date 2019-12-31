package com.daycold.account.avatar

import kotlin.reflect.KClass

/**
 * @author Stefan Liu
 */
interface UserInternalService<T : Avatar> {
    fun getUser(mobilePhone: String): T?
}

class UserBaseService(private val reflections: Map<KClass<out Avatar>, UserInternalService<out Avatar>>) {
    fun <T : Avatar> getUser(mobilePhone: String, targetAvatar: KClass<T>): T? {
        return reflections[targetAvatar]?.getUser(mobilePhone) as? T
    }
}

interface BusinessService : UserInternalService<Business>

interface CustomerService : UserInternalService<Customer>

