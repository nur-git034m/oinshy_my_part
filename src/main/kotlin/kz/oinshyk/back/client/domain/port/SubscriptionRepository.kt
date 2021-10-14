package kz.oinshyk.back.client.domain.port

import kz.oinshyk.back.client.domain.entity.Client
import kz.oinshyk.back.client.domain.entity.Subscription
import kz.oinshyk.back.payment.domain.entity.Payment
import org.springframework.data.repository.CrudRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import java.time.LocalDateTime

@RepositoryRestResource(exported = false)
interface SubscriptionRepository : CrudRepository<Subscription, Long> {

    fun findLastByClientAndValidUntilAfterOrderByValidUntil(client: Client, now: LocalDateTime?): Subscription?

    fun findByPayment(payment: Payment): Subscription?
}
