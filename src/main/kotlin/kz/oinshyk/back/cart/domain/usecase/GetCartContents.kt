package kz.oinshyk.back.cart.domain.usecase

import kz.oinshyk.back.cart.domain.entity.CartItem
import kz.oinshyk.back.cart.domain.port.CartItemRepository
import kz.oinshyk.back.cart.domain.port.CartRepository
import kz.oinshyk.back.client.domain.service.FindClient
import kz.oinshyk.back.dictionary.domain.usecase.GetDeliveryPrice
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class GetCartContents(
    private val findClient: FindClient,
    private val getDeliveryPrice: GetDeliveryPrice,
    private val cartRepository: CartRepository,
    private val cartItemRepository: CartItemRepository
) {

    fun contents(phoneNumber: String, key: String): CartDto {
        val client = findClient.find(phoneNumber, key)

        val cart = cartRepository.findByClient(client) ?: return CartDto(emptyList(), BigDecimal.ZERO, BigDecimal.ZERO)

        val items = cartItemRepository.findByCart(cart)

        val deliveryPrice = getDeliveryPrice.price()

        return CartDto(
            items,
            deliveryPrice,
            items.map { it.price * BigDecimal(it.quantity) }.reduce { a, i -> a + i } + deliveryPrice
        )
    }
}

data class CartDto(
    val items: Iterable<CartItem>,
    val deliveryPrice: BigDecimal,
    val totalAmount: BigDecimal
)
