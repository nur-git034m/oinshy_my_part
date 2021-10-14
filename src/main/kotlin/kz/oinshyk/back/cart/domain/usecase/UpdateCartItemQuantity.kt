package kz.oinshyk.back.cart.domain.usecase

import kz.oinshyk.back.cart.app.web.dto.UpdateCartItemQuantityDto
import kz.oinshyk.back.cart.domain.port.CartItemRepository
import kz.oinshyk.back.cart.domain.port.CartRepository
import kz.oinshyk.back.client.domain.service.FindClient
import org.springframework.stereotype.Service

@Service
class UpdateCartItemQuantity(
        private val findClient: FindClient,
        private val getCartContents: GetCartContents,
        private val cartRepository: CartRepository,
        private val cartItemRepository: CartItemRepository
) {

    fun update(dto: UpdateCartItemQuantityDto, itemId: Long): CartDto {
        val client = findClient.find(dto.phoneNumber, dto.key)
        val cart = cartRepository.findByClient(client) ?: throw CartNotFoundException()
        val cartItem = cartItemRepository.findByIdAndCart(itemId, cart) ?: throw CartItemNotFoundException()
        cartItem.quantity = dto.quantity
        cartItemRepository.save(cartItem)

        return getCartContents.contents(dto.phoneNumber, dto.key)
    }
}
