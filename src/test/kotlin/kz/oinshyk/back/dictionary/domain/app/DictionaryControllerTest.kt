package kz.oinshyk.back.dictionary.domain.app

import kz.oinshyk.back.BaseMvcIntegrationTest
import kz.oinshyk.back.dictionary.domain.entity.City
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.get

internal class DictionaryControllerTest : BaseMvcIntegrationTest() {
    @Test
    @Sql("/scripts/orders.sql")
    fun `Get cities`() {
        mvc.get("/dictionary/cities").andExpect {
            status { isOk }
            jsonPath("\$", hasSize<City>(1))
            jsonPath("\$[0].id") { value(1) }
        }
    }
}
