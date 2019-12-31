package com.daycold.account.client.service

import com.daycold.account.client.model.request.SmsAlarmRequest
import org.joda.time.DateTime

/**
 * @author Stefan Liu
 */
interface SmsApiService {
    fun batchAlaram(requests: Collection<SmsAlarmRequest>)

    fun delayBatchAlarm(requests: Collection<SmsAlarmRequest>, datetime: DateTime)
}