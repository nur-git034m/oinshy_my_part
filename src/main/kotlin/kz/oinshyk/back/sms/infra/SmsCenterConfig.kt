package kz.oinshyk.back.sms.infra

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("app.sms-center")
data class SmsCenterConfig(
        val url: String,
        val login: String,
        val password: String
)
