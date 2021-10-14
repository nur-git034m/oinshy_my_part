package kz.oinshyk.back.exchange.app

import kz.oinshyk.back.BaseMvcIntegrationTest
import kz.oinshyk.back.exchange.domain.usecase.ToyDto
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import org.springframework.web.util.NestedServletException
import java.math.BigDecimal
import javax.validation.ConstraintViolationException

@WithMockUser(roles = ["EXCHANGER"])
internal class ExchangeControllerTest : BaseMvcIntegrationTest() {

    @Test
    fun `SKU is empty`() {
        val exception = assertThrows<NestedServletException> {
            mvc.post("/exchange") {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(
                    ToyDto(
                        "",
                        "name",
                        "description",
                        BigDecimal(123),
                        BigDecimal(123),
                        10
                    )
                )
            }
        }
        Assertions.assertThat(exception.cause).isInstanceOf(ConstraintViolationException::class.java)
        Assertions.assertThat(exception.cause?.message).isEqualTo("create.dto.sku: must not be blank")
    }

    @Test
    fun `Price is negative`() {
        val exception = assertThrows<NestedServletException> {
            mvc.post("/exchange") {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(
                    ToyDto(
                        "sku",
                        "name",
                        "description",
                        BigDecimal(-1),
                        BigDecimal(123),
                        10
                    )
                )
            }
        }
        Assertions.assertThat(exception.cause).isInstanceOf(ConstraintViolationException::class.java)
        Assertions.assertThat(exception.cause?.message).isEqualTo("create.dto.price: must be greater than 0")
    }

    @Test
    fun `Price is zero`() {
        val exception = assertThrows<NestedServletException> {
            mvc.post("/exchange") {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(
                    ToyDto(
                        "sku",
                        "name",
                        "description",
                        BigDecimal(0),
                        BigDecimal(123),
                        10
                    )
                )
            }
        }
        Assertions.assertThat(exception.cause).isInstanceOf(ConstraintViolationException::class.java)
        Assertions.assertThat(exception.cause?.message).isEqualTo("create.dto.price: must be greater than 0")
    }

    @Test
    fun `Quantity is negative`() {
        val exception = assertThrows<NestedServletException> {
            mvc.post("/exchange") {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(
                    ToyDto(
                        "sku",
                        "name",
                        "description",
                        BigDecimal(1234),
                        BigDecimal(123),
                        -1
                    )
                )
            }
        }
        Assertions.assertThat(exception.cause).isInstanceOf(ConstraintViolationException::class.java)
        Assertions.assertThat(exception.cause?.message)
            .isEqualTo("create.dto.quantity: must be greater than or equal to 0")
    }

    @Test
    @Sql("/scripts/catalog.sql")
    fun `Create a new toy`() {
        mvc.post("/exchange") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(
                ToyDto(
                    "sku",
                    "name",
                    "description",
                    BigDecimal(1234),
                    BigDecimal(123),
                    10
                )
            )
        }.andExpect {
            status { isOk }
            jsonPath("result") { value("Ok") }
        }
    }

    @Test
    @Sql("/scripts/catalog.sql")
    fun `Update a toy`() {
        mvc.patch("/exchange") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(
                ToyDto(
                    "12345",
                    "name",
                    "description",
                    BigDecimal(1234),
                    BigDecimal(123),
                    10
                )
            )
        }.andExpect {
            status { isOk }
            jsonPath("result") { value("Ok") }
        }
    }
}
