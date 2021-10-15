package kz.oinshyk.back

import kz.oinshyk.back.catalog.infra.S3StorageConfig
import kz.oinshyk.back.common.infra.NotificationConfig
import kz.oinshyk.back.payment.infra.CloudPaymentsConfig
import kz.oinshyk.back.sms.infra.SmsCenterConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableAsync
import java.time.Clock

@SpringBootApplication
@EnableConfigurationProperties(
    AppConfig::class,
    SmsCenterConfig::class,
    CloudPaymentsConfig::class,
    S3StorageConfig::class,
    NotificationConfig::class
)
@EnableAsync
class BackApplication {

    @Bean
    fun rest(restTemplateBuilder: RestTemplateBuilder) = restTemplateBuilder.build()!!

    @Bean
    fun clock() = Clock.systemDefaultZone()!!

}

@ConstructorBinding
@ConfigurationProperties("app")
data class AppConfig(
    val env: String,
    val testPhoneNumber: String,
    val testPin: String
)

fun main(args: Array<String>) {
    runApplication<BackApplication>(*args)
}
