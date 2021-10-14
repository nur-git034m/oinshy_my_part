package kz.oinshyk.back.client.app.web

import kz.oinshyk.back.BaseMvcIntegrationTest
import kz.oinshyk.back.client.domain.entity.ClientOrder
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.get

internal class AdminClientInfoTest : BaseMvcIntegrationTest() {

    @Test
    @Sql("/scripts/client-registration.sql")
    fun `Find a client by an id - 401`() {
        mvc.get("/v1/clients/1").andExpect {
            status { isUnauthorized }
        }
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    @Sql("/scripts/client-registration.sql")
    fun `Find a client by an id - 404`() {
        mvc.get("/v1/clients/10").andExpect {
            status { isNotFound }
        }
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    @Sql("/scripts/client-registration.sql")
    fun `Find a client by an id`() {
        mvc.get("/v1/clients/1").andExpect {
            status { isOk }
            jsonPath("\$.phoneNumber") { value("12345678901") }
            jsonPath("\$.name") { value("Jon Dow") }
            jsonPath("\$.children") { value(1) }
            jsonPath("\$.key") { doesNotExist() }
            jsonPath("\$.temporalKey") { doesNotExist() }
            jsonPath("\$.pin") { doesNotExist() }
        }
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    @Sql("/scripts/orders.sql")
    fun `Get a client info`() {
        mvc.get("/client/admin-info/1").andExpect {
            status { isOk }
            jsonPath("\$.client.phoneNumber") { value("12345678901") }
            jsonPath("\$.client.children") { value(1) }
            jsonPath("\$.hasSubscription") { value(true) }
            jsonPath("\$.orders", hasSize<ClientOrder>(3))
        }
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    @Sql("/scripts/orders.sql")
    fun `Get a client info - 404`() {
        mvc.get("/client/admin-info/10").andExpect { status { isNotFound } }
    }
}
