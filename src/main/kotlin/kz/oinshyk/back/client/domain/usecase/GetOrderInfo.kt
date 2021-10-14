package kz.oinshyk.back.client.domain.usecase

import kz.oinshyk.back.client.domain.entity.ClientOrder
import kz.oinshyk.back.client.domain.port.ClientOrderRepository
import kz.oinshyk.back.client.domain.port.OrderItemRepository
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.ResponseStatus

@Service
@PreAuthorize("hasRole('ADMIN')")
class GetOrderInfo(
    private val orderRepository: ClientOrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val getClientInfo: GetClientInfo
) {

    fun get(id: Long): OrderInfoDto {
        val order = orderRepository.findById(id).orElseThrow { throw OrderNotFoundException() }
        return OrderInfoDto(
            getClientInfo.get(order.client),
            order.apply { items = orderItemRepository.findByOrder(this) }
        )
    }
}

@ResponseStatus(HttpStatus.NOT_FOUND)
class OrderNotFoundException : RuntimeException()

data class OrderInfoDto(
    val clientInfo: ClientInfoWithOrdersDto,
    val order: ClientOrder
)
