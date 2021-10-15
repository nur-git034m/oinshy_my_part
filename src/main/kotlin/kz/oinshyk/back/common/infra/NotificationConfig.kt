package kz.oinshyk.back.common.infra

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("app.notification")
data class NotificationConfig(
        val sender: String,
        val admins: List<String>
)
