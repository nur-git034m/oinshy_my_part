package kz.oinshyk.back.client.domain.usecase

import kz.oinshyk.back.client.domain.entity.ClientOrderStatus
import kz.oinshyk.back.client.domain.port.ClientOrderRepository
import kz.oinshyk.back.client.domain.port.OrderItemRepository
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
@PreAuthorize("hasRole('ADMIN')")
class FindOrdersByStatuses(
    private val orderRepository: ClientOrderRepository,
    private val orderItemRepository: OrderItemRepository
) {
    fun find(statuses: Set<ClientOrderStatus>) = orderRepository.findByStatusInOrderByOrderedAtDesc(statuses)
        .map {
            it.items = orderItemRepository.findByOrder(it)
            it
        }.toSet()
}
