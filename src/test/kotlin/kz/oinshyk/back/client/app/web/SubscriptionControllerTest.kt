package kz.oinshyk.back.client.app.web

import kz.oinshyk.back.BaseMvcIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.get
import java.time.LocalDate

internal class SubscriptionControllerTest : BaseMvcIntegrationTest() {

    @Test
    internal fun `Invalid phone number and key`() {
        mvc.get("/subscription/12345678909/1234xxx").andExpect {
            status { isUnauthorized }
        }
    }

    @Test
    @Sql("/scripts/subscription.sql")
    internal fun `No active subscription`() {
        mvc.get("/subscription/12345678901/123").andExpect {
            status { isNotFound }
        }
    }

    @Test
    @Sql("/scripts/subscription.sql")
    fun `Get the active one`() {
        mvc.get("/subscription/12345678902/123").andExpect {
            status { isOk }
            jsonPath("\$.validUntil") {
                value("${LocalDate.now().plusDays(1).atStartOfDay()}:00")
            }
            jsonPath("\$.savedAmount") { value(900) }
        }
    }

    @Test
    @Sql("/scripts/subscription.sql")
    fun `No orders yet`() {
        mvc.get("/subscription/12345678903/123").andExpect {
            status { isOk }
            jsonPath("\$.validUntil") {
                value("${LocalDate.now().plusDays(1).atStartOfDay()}:00")
            }
            jsonPath("\$.savedAmount") { value(0) }
        }
    }
}
