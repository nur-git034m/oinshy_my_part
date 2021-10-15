package kz.oinshyk.back.client.domain.usecase

import kz.oinshyk.back.client.domain.entity.ClientOrder
import kz.oinshyk.back.client.domain.port.ClientOrderRepository
import kz.oinshyk.back.client.domain.port.OrderItemRepository
import kz.oinshyk.back.client.domain.service.FindClient
import kz.oinshyk.back.security.infra.runWithRole
import org.springframework.stereotype.Service

@Service
class GetOrders(
    private val findClient: FindClient,
    private val orderRepository: ClientOrderRepository,
    private val orderItemRepository: OrderItemRepository
) {
    fun get(phoneNumber: String, key: String): Set<ClientOrder> {
        val client = findClient.find(phoneNumber, key)

        return runWithRole("ADMIN") {
            orderRepository.findByClientOrderByOrderedAtDesc(client)
                .map {
                    it.items = orderItemRepository.findByOrder(it)
                    it
                }.toSet()
        }
    }
}
