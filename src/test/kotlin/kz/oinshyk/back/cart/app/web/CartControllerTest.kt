package kz.oinshyk.back.cart.app.web

import kz.oinshyk.back.BaseMvcIntegrationTest
import kz.oinshyk.back.cart.app.web.dto.AddToyToCartDto
import kz.oinshyk.back.cart.app.web.dto.UpdateCartItemQuantityDto
import kz.oinshyk.back.cart.domain.entity.CartItem
import kz.oinshyk.back.cart.domain.port.CartItemRepository
import kz.oinshyk.back.cart.domain.port.CartRepository
import kz.oinshyk.back.common.domain.entity.BaseClientDto
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.MediaType
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import org.springframework.web.util.NestedServletException
import java.math.BigDecimal
import javax.validation.ConstraintViolationException

internal class CartControllerTest(
        private val cartRepository: CartRepository,
        private var cartItemRepository: CartItemRepository
) : BaseMvcIntegrationTest() {

    @Test
    internal fun `Add a toy - a phone number is empty`() {
        val exception = assertThrows<NestedServletException> {
            mvc.post("/cart") {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(AddToyToCartDto("", "123", 1L))
            }
        }
        assertThat(exception.cause).isInstanceOf(ConstraintViolationException::class.java)
        assertThat(exception.cause?.message).isEqualTo("addToy.addToyToCartDto.phoneNumber: must not be blank")
    }

    @Test
    internal fun `Add a toy - a key is empty`() {
        val exception = assertThrows<NestedServletException> {
            mvc.post("/cart") {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(AddToyToCartDto("12345678901", "", 1L))
            }
        }
        assertThat(exception.cause).isInstanceOf(ConstraintViolationException::class.java)
        assertThat(exception.cause?.message).isEqualTo("addToy.addToyToCartDto.key: must not be blank")
    }

    @Test
    internal fun `Add a toy - a client not found`() {
        mvc.post("/cart") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(AddToyToCartDto("123", "333", 1L))
        }.andExpect {
            status { isUnauthorized }
        }
    }

    @Test
    @Sql("/scripts/cart.sql")
    internal fun `Add a toy - an invalid key`() {
        mvc.post("/cart") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(AddToyToCartDto("12345678901", "333", 1L))
        }.andExpect {
            status { isUnauthorized }
        }
    }

    @Test
    @Sql("/scripts/cart.sql")
    internal fun `Add a toy - a toy not found`() {
        mvc.post("/cart") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(AddToyToCartDto("12345678901", "123", 100L))
        }.andExpect {
            status { isNotFound }
        }
    }

    @Test
    @Sql("/scripts/cart-empty.sql", "/scripts/settings.sql")
    fun `Add a toy into a new cart (without subscription)`() {
        assertThat(cartRepository.findAll()).isEmpty()

        val phoneNumber = "12345678901"

        mvc.post("/cart") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(AddToyToCartDto(phoneNumber, "123", 1L, 2))
        }.andExpect {
            status { isCreated }
            jsonPath("\$.items", hasSize<CartItem>(1))
            jsonPath("\$.totalAmount") {
                value(3000.0)
            }
        }

        assertThat(cartRepository.findAll()).hasSize(1)
        assertThat(cartRepository.findAll().first().client.phoneNumber).isEqualTo(phoneNumber)
        assertThat(cartItemRepository.findAll()).hasSize(1)
        assertThat(cartItemRepository.findAll().first().toy.id).isEqualTo(1L)
        assertThat(cartItemRepository.findAll().first().quantity).isEqualTo(2)
        assertThat(cartItemRepository.findAll().first().price).isEqualTo(BigDecimal(1000).setScale(2))
    }

    @Test
    @Sql("/scripts/cart.sql", "/scripts/settings.sql")
    fun `Add a toy into an existing cart (without subscription)`() {
        val phoneNumber = "12345678902"

        assertThat(cartRepository.findAll()).hasSize(1)
        assertThat(cartRepository.findAll().first().client.phoneNumber).isEqualTo(phoneNumber)
        assertThat(cartItemRepository.findAll()).hasSize(0)

        mvc.post("/cart") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(AddToyToCartDto(phoneNumber, "123", 1L, 2))
        }.andExpect {
            status { isCreated }
            jsonPath("\$.items", hasSize<CartItem>(1))
            jsonPath("\$.totalAmount") {
                value(3000.0)
            }
        }

        assertThat(cartItemRepository.findAll()).hasSize(1)
        assertThat(cartItemRepository.findAll().first().toy.id).isEqualTo(1L)
        assertThat(cartItemRepository.findAll().first().quantity).isEqualTo(2)
        assertThat(cartItemRepository.findAll().first().price).isEqualTo(BigDecimal(1000).setScale(2))
    }

    @Test
    @Sql("/scripts/cart-empty.sql", "/scripts/settings.sql")
    fun `Add a toy into a new cart (with subscription)`() {
        assertThat(cartRepository.findAll()).isEmpty()

        val phoneNumber = "12345678902"

        mvc.post("/cart") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(AddToyToCartDto(phoneNumber, "123", 1L, 2))
        }.andExpect {
            status { isCreated }
            jsonPath("\$.items", hasSize<CartItem>(1))
            jsonPath("\$.totalAmount") {
                value(2400.0)
            }
        }

        assertThat(cartRepository.findAll()).hasSize(1)
        assertThat(cartRepository.findAll().first().client.phoneNumber).isEqualTo(phoneNumber)
        assertThat(cartItemRepository.findAll()).hasSize(1)
        assertThat(cartItemRepository.findAll().first().toy.id).isEqualTo(1L)
        assertThat(cartItemRepository.findAll().first().quantity).isEqualTo(2)
        assertThat(cartItemRepository.findAll().first().price).isEqualTo(BigDecimal(700).setScale(2))
    }

    @Test
    internal fun `Get cart content - invalid phone number`() {
        mvc.get("/cart/12345678901/123").andExpect {
            status { isUnauthorized }
        }
    }

    @Test
    @Sql("/scripts/cart-full.sql")
    internal fun `Get cart content - invalid key`() {
        mvc.get("/cart/12345678901/123x").andExpect {
            status { isUnauthorized }
        }
    }

    @Test
    @Sql("/scripts/cart-full.sql", "/scripts/settings.sql")
    internal fun `Get cart contents`() {
        mvc.get("/cart/12345678901/123").andExpect {
            status { isOk }
            jsonPath("\$.items", hasSize<CartItem>(2))
            jsonPath("\$.deliveryPrice") {
                value(1000.0)
            }
            jsonPath("\$.totalAmount") {
                value(4000.0)
            }
        }
    }

    @Test
    @Sql("/scripts/cart-full.sql")
    internal fun `Delete an item - unknown client`() {
        mvc.post("/cart/delete/1") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(BaseClientDto("12345678909", "123"))
        }.andExpect {
            status { isUnauthorized }
        }
    }

    @Test
    @Sql("/scripts/cart-full.sql")
    internal fun `Delete an item - invalid key`() {
        mvc.post("/cart/delete/1") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(BaseClientDto("12345678901", "123x"))
        }.andExpect {
            status { isUnauthorized }
        }
    }

    @Test
    @Sql("/scripts/cart-full.sql")
    internal fun `Delete a non existing item`() {
        mvc.post("/cart/delete/4000") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(BaseClientDto("12345678901", "123"))
        }.andExpect {
            status { isNotFound }
        }
    }

    @Test
    @Sql("/scripts/cart-full.sql")
    internal fun `Delete other's item`() {
        mvc.post("/cart/delete/3") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(BaseClientDto("12345678901", "123"))
        }.andExpect {
            status { isNotFound }
        }
    }

    @Test
    @Sql("/scripts/cart-full.sql", "/scripts/settings.sql")
    fun `Delete an item`() {
        assertThat(cartItemRepository.findAll()).hasSize(3)

        mvc.post("/cart/delete/1") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(BaseClientDto("12345678901", "123"))
        }.andExpect {
            status { isFound }
            jsonPath("\$.items", hasSize<CartItem>(1))
            jsonPath("\$.totalAmount") {
                value(1900.0)
            }
        }

        assertThat(cartItemRepository.findAll()).hasSize(2)
    }

    @Test
    @Sql("/scripts/cart-full.sql", "/scripts/settings.sql")
    fun `Delete last item`() {
        assertThat(cartRepository.findAll()).hasSize(2)
        assertThat(cartItemRepository.findAll()).hasSize(3)

        mvc.post("/cart/delete/3") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(BaseClientDto("12345678902", "123"))
        }.andExpect {
            status { isFound }
            jsonPath("\$.items", hasSize<CartItem>(0))
            jsonPath("\$.totalAmount") {
                value(0.0)
            }
        }

        assertThat(cartRepository.findAll()).hasSize(1)
        assertThat(cartRepository.findAll().first().id).isEqualTo(1)
        assertThat(cartItemRepository.findAll()).hasSize(2)
    }

    @Test
    @Sql("/scripts/cart-full.sql")
    internal fun `Delete cart - invalid phone number`() {
        assertThat(cartRepository.findAll()).hasSize(2)
        assertThat(cartItemRepository.findAll()).hasSize(3)

        mvc.post("/cart/delete") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(BaseClientDto("12345678909", "123"))
        }.andExpect {
            status { isUnauthorized }
        }

        assertThat(cartRepository.findAll()).hasSize(2)
        assertThat(cartItemRepository.findAll()).hasSize(3)
    }

    @Test
    @Sql("/scripts/cart-full.sql")
    internal fun `Delete cart - invalid key`() {
        assertThat(cartRepository.findAll()).hasSize(2)
        assertThat(cartItemRepository.findAll()).hasSize(3)

        mvc.post("/cart/delete") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(BaseClientDto("12345678901", "123x"))
        }.andExpect {
            status { isUnauthorized }
        }

        assertThat(cartRepository.findAll()).hasSize(2)
        assertThat(cartItemRepository.findAll()).hasSize(3)
    }

    @Test
    @Sql("/scripts/cart-full.sql")
    fun `Delete cart`() {
        assertThat(cartRepository.findAll()).hasSize(2)
        assertThat(cartItemRepository.findAll()).hasSize(3)

        mvc.post("/cart/delete") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(BaseClientDto("12345678901", "123"))
        }.andExpect {
            status { isFound }
        }

        assertThat(cartRepository.findAll()).hasSize(1)
        assertThat(cartRepository.findAll().first().id).isEqualTo(2)
        assertThat(cartItemRepository.findAll()).hasSize(1)
    }

    @Test
    @Sql("/scripts/cart-full.sql")
    internal fun `Update quantity - not found`() {
        mvc.put("/cart/3") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(UpdateCartItemQuantityDto("12345678901", "123", 2))
        }.andExpect {
            status { isNotFound }
        }
    }

    @Test
    @Sql("/scripts/cart-full.sql")
    internal fun `Update quantity - invalid quantity`() {
        val exception = assertThrows<NestedServletException> {
            mvc.put("/cart/2") {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(UpdateCartItemQuantityDto("12345678901", "123", 0))
            }
        }
        assertThat(exception.cause).isInstanceOf(ConstraintViolationException::class.java)
        assertThat(exception.cause?.message).isEqualTo("put.dto.quantity: must be greater than 0")
    }

    @Test
    @Sql("/scripts/cart-full.sql", "/scripts/settings.sql")
    internal fun `Update quantity`() {
        assertThat(cartItemRepository.findAll()).hasSize(3)
        assertThat(cartItemRepository.findById(2).get().quantity).isEqualTo(1)

        mvc.put("/cart/2") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(UpdateCartItemQuantityDto("12345678901", "123", 2))
        }.andExpect {
            status { isFound }
        }

        assertThat(cartItemRepository.findAll()).hasSize(3)
        assertThat(cartItemRepository.findById(2).get().quantity).isEqualTo(2)
    }
}
