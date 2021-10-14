package kz.oinshyk.back.client.app.web

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import kz.oinshyk.back.BaseMvcIntegrationTest
import kz.oinshyk.back.cart.domain.port.CartItemRepository
import kz.oinshyk.back.cart.domain.port.CartRepository
import kz.oinshyk.back.client.app.web.dto.OrderDto
import kz.oinshyk.back.client.domain.entity.ClientOrderPaymentType
import kz.oinshyk.back.client.domain.entity.ClientOrderStatus
import kz.oinshyk.back.client.domain.port.ClientOrderRepository
import kz.oinshyk.back.client.domain.port.OrderItemRepository
import kz.oinshyk.back.common.domain.entity.BaseClientDto
import kz.oinshyk.back.common.domain.port.DomainNotification
import kz.oinshyk.back.dictionary.domain.entity.City
import kz.oinshyk.back.security.infra.runWithRole
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import org.springframework.web.util.NestedServletException
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.validation.ConstraintViolationException

internal class OrderControllerTest(
    private val clientOrderRepository: ClientOrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val cartRepository: CartRepository,
    private val cartItemRepository: CartItemRepository
) : BaseMvcIntegrationTest() {

    @MockkBean
    private lateinit var domainNotification: DomainNotification

    @Test
    internal fun `Submit order - invalid phone number`() {
        val exception = org.junit.jupiter.api.assertThrows<NestedServletException> {
            mvc.post("/order") {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(
                    OrderDto(
                        "",
                        "123",
                        City("almaty").apply { id = 1 },
                        "Some st.",
                        "123 bld.",
                        "apt. 111",
                        ClientOrderPaymentType.UponDelivery
                    )
                )
            }
        }
        assertThat(exception.cause).isInstanceOf(ConstraintViolationException::class.java)
        assertThat(exception.cause?.message).isEqualTo("newOrder.dto.phoneNumber: must not be blank")
    }

    @Test
    internal fun `Submit order - invalid client`() {
        mvc.post("/order") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(
                OrderDto(
                    "12345678909",
                    "123",
                    City("almaty").apply { id = 1 },
                    "Some st.",
                    "123 bld.",
                    "apt. 111",
                    ClientOrderPaymentType.UponDelivery
                )
            )
        }.andExpect { status { isUnauthorized } }
    }

    @Test
    @Sql("/scripts/cart-full.sql")
    internal fun `Submit order - invalid key`() {
        mvc.post("/order") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(
                OrderDto(
                    "12345678901",
                    "123x",
                    City("almaty").apply { id = 1 },
                    "Some st.",
                    "123 bld.",
                    "apt. 111",
                    ClientOrderPaymentType.UponDelivery
                )
            )
        }.andExpect { status { isUnauthorized } }
    }

    @Test
    @Sql("/scripts/cart-full.sql")
    internal fun `Submit order - no cart`() {
        mvc.post("/order") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(
                OrderDto(
                    "12345678903",
                    "123",
                    City("almaty").apply { id = 1 },
                    "Some st.",
                    "123 bld.",
                    "apt. 111",
                    ClientOrderPaymentType.UponDelivery
                )
            )
        }.andExpect { status { isPreconditionFailed } }
    }

    @Test
    @Sql("/scripts/cart-full.sql")
    internal fun `Submit order - invalid city id`() {
        mvc.post("/order") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(
                OrderDto(
                    "12345678901",
                    "123",
                    City("almaty").apply { id = 100 },
                    "Some st.",
                    "123 bld.",
                    "apt. 111",
                    ClientOrderPaymentType.UponDelivery
                )
            )
        }.andExpect { status { isPreconditionFailed } }
    }

    @Test
    @Sql("/scripts/cart-full.sql", "/scripts/settings.sql")
    internal fun `Submit order - invalid street`() {
        val exception = org.junit.jupiter.api.assertThrows<NestedServletException> {
            mvc.post("/order") {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(
                    OrderDto(
                        "12345678901",
                        "123",
                        City("almaty").apply { id = 1 },
                        "",
                        "123 bld.",
                        "apt. 111",
                        ClientOrderPaymentType.UponDelivery
                    )
                )
            }
        }
        assertThat(exception.cause).isInstanceOf(ConstraintViolationException::class.java)
    }

    @Test
    @Sql("/scripts/cart-full.sql", "/scripts/settings.sql")
    fun `Submit order`() {
        runWithRole("ADMIN") {
            assertThat(clientOrderRepository.count()).isEqualTo(0)
        }
        assertThat(cartRepository.count()).isEqualTo(2)
        assertThat(cartItemRepository.count()).isEqualTo(3)

        every { domainNotification.byEmailOnNewOrder(any()) }.answers { }

        mvc.post("/order") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(
                OrderDto(
                    "12345678901",
                    "123",
                    City("almaty").apply { id = 1 },
                    "Some st.",
                    "123 bld.",
                    "apt. 111",
                    ClientOrderPaymentType.UponDelivery
                )
            )
        }.andExpect {
            status { isCreated }
            jsonPath("\$.id") { isNumber }
            jsonPath("\$.amount") { value(4000) }
        }

        verify { domainNotification.byEmailOnNewOrder(any()) }

        runWithRole("ADMIN") {
            assertThat(clientOrderRepository.count()).isEqualTo(1)
            assertThat(clientOrderRepository.findAll().first().client.phoneNumber).isEqualTo("12345678901")
            assertThat(clientOrderRepository.findAll().first().city.id).isEqualTo(1)
            assertThat(clientOrderRepository.findAll().first().street).isEqualTo("Some st.")
            assertThat(clientOrderRepository.findAll().first().building).isEqualTo("123 bld.")
            assertThat(clientOrderRepository.findAll().first().apartment).isEqualTo("apt. 111")
            assertThat(clientOrderRepository.findAll().first().items).hasSize(2)
            assertThat(clientOrderRepository.findAll().first().orderedAt).isEqualToIgnoringSeconds(LocalDateTime.now())
            assertThat(clientOrderRepository.findAll().first().paid).isEqualTo(false)
            assertThat(
                clientOrderRepository.findAll().first().paymentType
            ).isEqualTo(ClientOrderPaymentType.UponDelivery)
            assertThat(clientOrderRepository.findAll().first().deliveryPrice).isEqualTo(BigDecimal("1000.00"))
            assertThat(clientOrderRepository.findAll().first().status).isEqualTo(ClientOrderStatus.Ordered)
        }

        assertThat(orderItemRepository.count()).isEqualTo(2)
        assertThat(orderItemRepository.findAll()
            .map { it.originalPrice * BigDecimal(it.quantity) }
            .reduce { a, amount -> a + amount })
            .isEqualTo(BigDecimal(4200).setScale(2))

        assertThat(cartRepository.count()).isEqualTo(1)
        assertThat(cartItemRepository.count()).isEqualTo(1)
    }

    @Test
    @Sql("/scripts/orders.sql")
    fun `Cancel order - invalid phone number`() {
        val exception = org.junit.jupiter.api.assertThrows<NestedServletException> {
            mvc.patch("/order/1/cancel") {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(
                    BaseClientDto(
                        "",
                        "123"
                    )
                )
            }.andExpect {
                status { isOk }
            }
        }
        assertThat(exception.cause).isInstanceOf(ConstraintViolationException::class.java)
        assertThat(exception.cause?.message).isEqualTo("cancel.dto.phoneNumber: must not be blank")
    }

    @Test
    @Sql("/scripts/orders.sql")
    fun `Cancel order - client not found`() {
        mvc.patch("/order/1/cancel") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(
                BaseClientDto(
                    "12345678909",
                    "123"
                )
            )
        }.andExpect {
            status { isUnauthorized }
        }
    }

    @Test
    @Sql("/scripts/orders.sql")
    fun `Cancel order - wrong key`() {
        mvc.patch("/order/1/cancel") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(
                BaseClientDto(
                    "12345678901",
                    "123x"
                )
            )
        }.andExpect {
            status { isUnauthorized }
        }
    }

    @Test
    @Sql("/scripts/orders.sql")
    fun `Cancel order - it's not yours!`() {
        mvc.patch("/order/2/cancel") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(
                BaseClientDto(
                    "12345678901",
                    "123"
                )
            )
        }.andExpect {
            status { isNotFound }
        }
    }

    @Test
    @Sql("/scripts/orders.sql")
    fun `Cancel order - not found`() {
        mvc.patch("/order/3/cancel") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(
                BaseClientDto(
                    "12345678901",
                    "123"
                )
            )
        }.andExpect {
            status { isNotFound }
        }
    }

    @Test
    @Sql("/scripts/orders.sql")
    fun `Cancel order - invalid status`() {
        runWithRole("ADMIN") {
            assertThat(clientOrderRepository.findById(1).get().status).isEqualTo(ClientOrderStatus.Delivered)
        }

        mvc.patch("/order/1/cancel") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(
                BaseClientDto(
                    "12345678901",
                    "123"
                )
            )
        }.andExpect {
            status { isNotFound }
        }
    }

    @Test
    @Sql("/scripts/orders.sql")
    fun `Cancel order - already paid`() {
        runWithRole("ADMIN") {
            assertThat(clientOrderRepository.findById(4).get().paid).isEqualTo(true)
        }

        mvc.patch("/order/4/cancel") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(
                BaseClientDto(
                    "12345678901",
                    "123"
                )
            )
        }.andExpect {
            status { isNotFound }
        }
    }

    @Test
    @Sql("/scripts/orders.sql")
    fun `Cancel order`() {
        runWithRole("ADMIN") {
            assertThat(clientOrderRepository.findById(5).get().status).isEqualTo(ClientOrderStatus.Ordered)
            assertThat(clientOrderRepository.findById(5).get().paid).isEqualTo(false)
        }

        mvc.patch("/order/5/cancel") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(
                BaseClientDto(
                    "12345678901",
                    "123"
                )
            )
        }.andExpect {
            status { isOk }
        }

        runWithRole("ADMIN") {
            assertThat(clientOrderRepository.findById(5).get().status).isEqualTo(ClientOrderStatus.Cancelled)
        }
    }
}
