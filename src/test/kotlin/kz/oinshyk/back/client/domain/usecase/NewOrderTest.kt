package kz.oinshyk.back.client.domain.usecase

import com.ninjasquad.springmockk.MockkBean
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verifySequence
import kz.oinshyk.back.cart.domain.entity.Cart
import kz.oinshyk.back.cart.domain.entity.CartItem
import kz.oinshyk.back.cart.domain.port.CartRepository
import kz.oinshyk.back.catalog.domain.entity.Category
import kz.oinshyk.back.catalog.domain.entity.Toy
import kz.oinshyk.back.client.app.web.dto.OrderDto
import kz.oinshyk.back.client.domain.entity.*
import kz.oinshyk.back.client.domain.port.ClientOrderRepository
import kz.oinshyk.back.client.domain.port.OrderItemRepository
import kz.oinshyk.back.client.domain.service.FindClient
import kz.oinshyk.back.common.domain.port.DomainNotification
import kz.oinshyk.back.dictionary.domain.entity.City
import kz.oinshyk.back.dictionary.domain.port.CityRepository
import kz.oinshyk.back.dictionary.domain.usecase.GetDeliveryPrice
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@ExtendWith(MockKExtension::class)
internal class NewOrderTest {

    @MockK
    lateinit var findClient: FindClient

    @MockK
    lateinit var getDeliveryPrice: GetDeliveryPrice

    @MockK
    lateinit var cartRepository: CartRepository

    @MockK
    lateinit var clientOrderRepository: ClientOrderRepository

    @MockK
    lateinit var orderItemRepository: OrderItemRepository

    @MockK
    lateinit var cityRepository: CityRepository

    @MockK
    lateinit var domainNotification: DomainNotification

    @MockkBean
    var clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())!!

    @Test
    fun `New order`() {
        val client = Client("123", 0, "Jon")

        every { findClient.find("123", "key") } returns client

        val toy = Toy(
            "sku",
            "toy 1",
            BigDecimal(1200),
            BigDecimal(1000),
            10,
            Category("Cat 1", "img")
        )
        val cart = Cart(client, mutableListOf())
            .apply { items.add(CartItem(this, toy, BigDecimal(1200), 1)) }
        every { cartRepository.findByClient(client) } returns cart

        every { cityRepository.findById(1) } returns Optional.of(City("Almaty"))

        every { getDeliveryPrice.price() } returns BigDecimal(1000)

        val order = ClientOrder(
            client,
            ClientOrderPaymentType.UponDelivery,
            ClientOrderType.Online,
            BigDecimal(1000),
            City("Almaty").apply { id = 1 },
            "street",
            "building",
            LocalDateTime.now(clock),
            "apartment"
        )
        every { clientOrderRepository.save(order) } returns order

        val orderItem = OrderItem(
            order,
            toy,
            BigDecimal(1200),
            BigDecimal(1200),
            1
        )
        every { orderItemRepository.save(orderItem) } returns orderItem

        every { cartRepository.delete(cart) } returns Unit

        every { orderItemRepository.findByOrder(order) } returns mutableListOf(orderItem)

        every { domainNotification.byEmailOnNewOrder(order) } returns Unit

        val responseDto = NewOrder(
            findClient,
            getDeliveryPrice,
            cartRepository,
            clientOrderRepository,
            orderItemRepository,
            cityRepository,
            domainNotification,
            clock
        ).order(
            OrderDto(
                "123",
                "key",
                City("Almaty").apply { id = 1 },
                "street",
                "building",
                "apartment",
                ClientOrderPaymentType.UponDelivery
            )
        )

        assertThat(responseDto).isEqualTo(NewOrderResponseDto(null, BigDecimal(2200)))

        verifySequence {
            findClient.find("123", "key")
            cartRepository.findByClient(client)
            cityRepository.findById(1)
            getDeliveryPrice.price()
            clientOrderRepository.save(order)
            orderItemRepository.save(orderItem)
            cartRepository.delete(cart)
            domainNotification.byEmailOnNewOrder(order)
        }

        confirmVerified(
            findClient,
            getDeliveryPrice,
            cartRepository,
            clientOrderRepository,
            orderItemRepository,
            cityRepository,
            domainNotification
        )
    }
}
