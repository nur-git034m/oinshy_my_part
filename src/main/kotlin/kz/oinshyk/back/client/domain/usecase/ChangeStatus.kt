package kz.oinshyk.back.client.domain.usecase

import kz.oinshyk.back.client.app.web.ChangeStatusDto
import kz.oinshyk.back.client.domain.port.ClientOrderRepository
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
@PreAuthorize("hasRole('ADMIN')")
class ChangeStatus(
        private val orderRepository: ClientOrderRepository
) {

    fun update(id: Long, dto: ChangeStatusDto) {
        val order = orderRepository.findById(id).orElseThrow() { throw ClientOrderNotFound() }
        order.status = dto.status
        order.declineReason = dto.declineReason
        order.paid = dto.paid
        orderRepository.save(order)
    }

}
