package kz.oinshyk.back.client.app.web

import kz.oinshyk.back.BaseMvcIntegrationTest
import kz.oinshyk.back.client.domain.entity.ClientOrder
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.get

internal class OrdersControllerTest : BaseMvcIntegrationTest() {

    @Test
    @Sql("/scripts/orders.sql")
    fun `Get orders - invalid phone number`() {
        mvc.get("/orders/12345678909/123").andExpect { status { isUnauthorized } }
    }

    @Test
    @Sql("/scripts/orders.sql")
    fun `Get orders - invalid key`() {
        mvc.get("/orders/12345678901/123x").andExpect { status { isUnauthorized } }
    }

    @Test
    @Sql("/scripts/orders.sql")
    fun `Get orders`() {
        mvc.get("/orders/12345678901/123").andExpect {
            status { isOk }
            jsonPath("\$", hasSize<ClientOrder>(3))
            jsonPath("\$[0].id") { value(1) }
            jsonPath("\$[0].totalAmount") { value(3300) }
        }
    }
}
