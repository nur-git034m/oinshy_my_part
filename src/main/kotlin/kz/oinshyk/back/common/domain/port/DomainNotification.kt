package kz.oinshyk.back.common.domain.port

import kz.oinshyk.back.client.domain.entity.ClientOrder
import org.springframework.scheduling.annotation.Async
import org.springframework.transaction.event.TransactionalEventListener

interface DomainNotification {

    fun byEmailOnNewOrder(order: ClientOrder)

    @Async
    @TransactionalEventListener
    fun sendEmail(order: ClientOrder)

}
