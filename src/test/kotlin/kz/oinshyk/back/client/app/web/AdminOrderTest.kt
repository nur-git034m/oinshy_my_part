package kz.oinshyk.back.client.app.web

import kz.oinshyk.back.BaseMvcIntegrationTest
import kz.oinshyk.back.client.domain.entity.ClientOrder
import kz.oinshyk.back.client.domain.entity.ClientOrderStatus
import kz.oinshyk.back.client.domain.port.ClientOrderRepository
import kz.oinshyk.back.security.infra.runWithRole
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import java.math.BigDecimal

internal class AdminOrderTest(
        private val orderRepository: ClientOrderRepository
) : BaseMvcIntegrationTest() {

    @Test
    @Sql("/scripts/orders.sql")
    fun `Find an order - 401`() {
        mvc.get("/order/admin-info/1").andExpect { status { isUnauthorized } }
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    @Sql("/scripts/orders.sql")
    fun `Find an order - 404`() {
        mvc.get("/order/admin-info/10").andExpect { status { isNotFound } }
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    @Sql("/scripts/orders.sql")
    fun `Get an order info`() {
        mvc.get("/order/admin-info/1").andExpect {
            status { isOk }
            jsonPath("\$.clientInfo.client.phoneNumber") { value("12345678901") }
            jsonPath("\$.clientInfo.client.children") { value(1) }
            jsonPath("\$.clientInfo.hasSubscription") { value(true) }
            jsonPath("\$.clientInfo.orders", hasSize<ClientOrder>(3))
            jsonPath("\$.order.items", hasSize<ClientOrder>(2))
            jsonPath("\$.order.totalAmount") { value(BigDecimal("3300.0")) }
        }
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    @Sql("/scripts/orders.sql")
    fun `Find orders`() {
        mvc.get("/orders?statuses=Ordered").andExpect {
            status { isOk }
            jsonPath("\$", hasSize<ClientOrder>(3))
            jsonPath("\$[0].client.phoneNumber") { exists() }
            jsonPath("\$[0].city.name") { exists() }
        }
    }

    @Test
    fun `Set status - 401`() {
        mvc.patch("/order/1/set-status") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(ChangeStatusDto(
                    ClientOrderStatus.Cancelled,
                    "xxx",
                    false
            ))
        }.andExpect { status { isUnauthorized } }
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    @Sql("/scripts/orders.sql")
    fun `Set status - 404`() {
        mvc.patch("/order/10/set-status") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(ChangeStatusDto(
                    ClientOrderStatus.Cancelled,
                    "xxx",
                    false
            ))
        }.andExpect { status { isNotFound } }
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    @Sql("/scripts/orders.sql")
    fun `Set status`() {
        orderRepository.findById(4).orElseThrow().also {
            assertThat(it.status).isEqualTo(ClientOrderStatus.Ordered)
            assertThat(it.declineReason).isNull()
            assertThat(it.paid).isTrue()
        }

        mvc.patch("/order/4/set-status") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(ChangeStatusDto(
                    ClientOrderStatus.Cancelled,
                    "xxx",
                    false
            ))
        }.andExpect {
            status { isOk }
        }

        runWithRole("ADMIN") {
            orderRepository.findById(4).orElseThrow().also {
                assertThat(it.status).isEqualTo(ClientOrderStatus.Cancelled)
                assertThat(it.declineReason).isEqualTo("xxx")
                assertThat(it.paid).isFalse()
            }
        }
    }
}
