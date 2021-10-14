package kz.oinshyk.back.dictionary.domain

import kz.oinshyk.back.BaseMvcIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.get

internal class GlobalParamsControllerTest : BaseMvcIntegrationTest() {

    @Test
    fun `Delivery price - not set`() {
        mvc.get("/params/delivery-price").andExpect { status { isPreconditionFailed } }
    }

    @Test
    @Sql("/scripts/settings.sql")
    fun `Delivery price`() {
        mvc.get("/params/delivery-price").andExpect {
            status { isOk }
            content { string("1000.00") }
        }
    }

    @Test
    fun `Subscription price - not set`() {
        mvc.get("/params/subscription-price").andExpect { status { isPreconditionFailed } }
    }

    @Test
    @Sql("/scripts/settings.sql")
    fun `Subscription price`() {
        mvc.get("/params/subscription-price").andExpect {
            status { isOk }
            content { string("5000.00") }
        }
    }
}
