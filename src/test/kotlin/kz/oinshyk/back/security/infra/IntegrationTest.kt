package kz.oinshyk.back.security.infra

import kz.oinshyk.back.catalog.domain.port.ToyRepository
import kz.oinshyk.back.client.domain.port.OrderItemRepository
import kz.oinshyk.back.exchange.domain.usecase.ToyDto
import kz.oinshyk.back.user.domain.port.AppUserRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlMergeMode
import java.math.BigDecimal

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@Sql("/scripts/users.sql")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class IntegrationTest(
    private val rest: TestRestTemplate,
    private val appUserRepository: AppUserRepository,
    private val toyRepository: ToyRepository,
    private val orderItemRepository: OrderItemRepository
) {
    private val apiVersion = "v1"

    @AfterEach
    fun tearDown() {
        runWithRole("ADMIN") {
            appUserRepository.deleteAll()
            orderItemRepository.deleteAll()
            toyRepository.deleteAll()
        }
    }

    @Test
    fun `Should not have access to app users`() {
        val response = rest.getForEntity<String>("/$apiVersion/appUsers")
        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun `Should have access to app users`() {
        val response = rest.withBasicAuth("admin", "xxx")
            .getForEntity<String>("/$apiVersion/appUsers")
        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    @Sql("/scripts/orders.sql")
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    fun `Get orders by Admin`() {
        val response = rest.withBasicAuth("admin", "xxx")
            .getForEntity<String>("/$apiVersion/order/admin-info/1")
        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `Should not have access to an exchange`() {
        val response = rest.postForEntity<String>(
            "/$apiVersion/exchange",
            HttpEntity(
                ToyDto(
                    "sku",
                    "name",
                    "desc",
                    BigDecimal(1200),
                    BigDecimal(1000),
                    10
                ),
                HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
            )
        )
        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun `Should have access to an exchange`() {
        val response = rest.withBasicAuth("exchanger", "xxx")
            .postForEntity<String>("/$apiVersion/exchange",
                HttpEntity(
                    ToyDto(
                        "sku",
                        "name",
                        "desc",
                        BigDecimal(1200),
                        BigDecimal(1000),
                        10
                    ),
                    HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
                )
            )
        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }
}
