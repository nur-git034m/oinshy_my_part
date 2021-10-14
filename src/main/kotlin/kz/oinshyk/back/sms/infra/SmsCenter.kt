package kz.oinshyk.back.sms.infra

import kz.oinshyk.back.sms.domain.port.SmsProvider
import kz.oinshyk.back.sms.infra.dto.SmsCenterResponseDto
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange

@Service
class SmsCenter(
        private val config: SmsCenterConfig,
        private val restTemplate: RestTemplate
) : SmsProvider {

    override fun sendPin(phoneNumber: String, code: String) {
        val message = "ToyVille: $code"
        val url = "${config.url}?login=${config.login}&psw=${config.password}&fmt=3&charset=utf-8&phones=$phoneNumber&mes=$message"
        val response = restTemplate.exchange<SmsCenterResponseDto>(url, HttpMethod.POST)
        if (response.body?.error_code != null) throw SmsCenterException(response.body!!)
    }
}

data class SmsCenterException(val body: SmsCenterResponseDto) : Throwable()
