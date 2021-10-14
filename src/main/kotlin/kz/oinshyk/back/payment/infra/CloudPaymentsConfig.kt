package kz.oinshyk.back.payment.infra

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("app.cloud-payments")
data class CloudPaymentsConfig(
        val apiUrl: String,
        val publicId: String,
        val apiSecret: String
)
