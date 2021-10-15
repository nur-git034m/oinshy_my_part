package kz.oinshyk.back.client.domain.service

import kz.oinshyk.back.client.domain.entity.Client
import kz.oinshyk.back.client.domain.entity.ClientOrderStatus
import kz.oinshyk.back.client.domain.port.ClientOrderRepository
import kz.oinshyk.back.client.domain.port.OrderItemRepository
import kz.oinshyk.back.client.domain.port.SubscriptionRepository
import kz.oinshyk.back.security.infra.runWithRole
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.ResponseStatus
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class FindSubscription(
    private val findClient: FindClient,
    private val repository: SubscriptionRepository,
    private val orderRepository: ClientOrderRepository,
    private val orderItemRepository: OrderItemRepository
) {

    fun find(phoneNumber: String, key: String): SubscriptionDto {
        val client = findClient.find(phoneNumber, key)
        val subscription = find(client)

        val savedAmount = runWithRole {
            orderRepository.findByClientAndStatusNot(client, ClientOrderStatus.Cancelled)
                .map {
                    orderItemRepository.findByOrder(it)
                        .map { i ->
                            (i.originalPrice - i.price).multiply(BigDecimal(i.quantity))
                        }
                        .fold(BigDecimal.ZERO) { a, s -> a + s }
                }
                .fold(BigDecimal.ZERO) { a, s -> a + s }
        }

        return SubscriptionDto(subscription.validUntil, savedAmount)
    }

    fun find(client: Client) =
        repository.findLastByClientAndValidUntilAfterOrderByValidUntil(client, LocalDateTime.now())
            ?: throw SubscriptionNotFoundException()

    fun exists(client: Client) =
        repository.findLastByClientAndValidUntilAfterOrderByValidUntil(client, LocalDateTime.now()) != null
}

data class SubscriptionDto(
    val validUntil: LocalDateTime,
    val savedAmount: BigDecimal
)

@ResponseStatus(HttpStatus.NOT_FOUND)
class SubscriptionNotFoundException : RuntimeException()

