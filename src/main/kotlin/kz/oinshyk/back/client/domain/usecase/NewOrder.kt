package kz.oinshyk.back.client.domain.usecase

import kz.oinshyk.back.cart.domain.port.CartRepository
import kz.oinshyk.back.client.app.web.dto.OrderDto
import kz.oinshyk.back.client.domain.entity.ClientOrder
import kz.oinshyk.back.client.domain.entity.ClientOrderType
import kz.oinshyk.back.client.domain.entity.OrderItem
import kz.oinshyk.back.client.domain.port.ClientOrderRepository
import kz.oinshyk.back.client.domain.port.OrderItemRepository
import kz.oinshyk.back.client.domain.service.FindClient
import kz.oinshyk.back.common.domain.port.DomainNotification
import kz.oinshyk.back.dictionary.domain.port.CityRepository
import kz.oinshyk.back.dictionary.domain.usecase.GetDeliveryPrice
import kz.oinshyk.back.security.infra.runWithRole
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.ResponseStatus
import java.math.BigDecimal
import java.time.Clock
import java.time.LocalDateTime

@Service
class NewOrder(
    private val findClient: FindClient,
    private val getDeliveryPrice: GetDeliveryPrice,
    private val cartRepository: CartRepository,
    private val clientOrderRepository: ClientOrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val cityRepository: CityRepository,
    private val domainNotification: DomainNotification,
    private val clock: Clock
) {

    @Transactional
    fun order(dto: OrderDto): NewOrderResponseDto {
        val client = findClient.find(dto.phoneNumber, dto.key)
        val cart = cartRepository.findByClient(client) ?: throw NewOrderPreconditionException()
        val city = cityRepository.findById(dto.city.id!!).orElseThrow { throw CityNotFoundException() }
        val deliveryPrice = getDeliveryPrice.price()

        val order = ClientOrder(
            client = client,
            type = ClientOrderType.Online,
            paymentType = dto.paymentType,
            deliveryPrice = deliveryPrice,
            city = city,
            street = dto.street,
            building = dto.building,
            apartment = dto.apartment,
            orderedAt = LocalDateTime.now(clock)
        )
        runWithRole("ADMIN") {
            clientOrderRepository.save(order)
        }
        cart.items.forEach {
            val orderItem = OrderItem(order, it.toy, it.price, it.toy.price, it.quantity)
            orderItemRepository.save(orderItem)
            order.items.add(orderItem)
        }

        cartRepository.delete(cart)

        domainNotification.byEmailOnNewOrder(order)

        return NewOrderResponseDto(order.id, order.getTotalAmount())
    }
}

data class NewOrderResponseDto(val id: Long?, val amount: BigDecimal?)

@ResponseStatus(HttpStatus.PRECONDITION_FAILED)
class CityNotFoundException : RuntimeException()

@ResponseStatus(HttpStatus.PRECONDITION_FAILED)
class NewOrderPreconditionException : RuntimeException()
