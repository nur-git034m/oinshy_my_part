package kz.oinshyk.back.common.infra

import kz.oinshyk.back.client.domain.entity.ClientOrder
import kz.oinshyk.back.common.domain.port.DomainNotification
import org.springframework.context.ApplicationEventPublisher
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

@Suppress("unused")
@Service
class DomainNotificationImpl(
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val config: NotificationConfig,
    private val mailSender: JavaMailSender
) : DomainNotification {

    override fun byEmailOnNewOrder(order: ClientOrder) {
        applicationEventPublisher.publishEvent(order)
    }

    override fun sendEmail(order: ClientOrder) {
        val message = mailSender.createMimeMessage()
        MimeMessageHelper(message).apply {
            setFrom(config.sender)
            setTo(config.admins.toTypedArray())
            setSubject("Новый заказ")
            setText("Поступил новый заказ № ${order.id}.")
        }
        mailSender.send(message)
    }
}
