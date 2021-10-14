package kz.oinshyk.back.sms.domain.port

interface SmsProvider {

    fun sendPin(phoneNumber: String, code: String)
}
