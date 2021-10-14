package kz.oinshyk.back.cart.domain.usecase

import kz.oinshyk.back.cart.domain.port.CartItemRepository
import kz.oinshyk.back.cart.domain.port.CartRepository
import kz.oinshyk.back.client.domain.service.FindClient
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.ResponseStatus

@Service
class DeleteItemFromCart(
        private val findClient: FindClient,
        private val getCartContents: GetCartContents,
        private val cartRepository: CartRepository,
        private val cartItemRepository: CartItemRepository
) {

    @Transactional
    fun delete(phoneNumber: String, key: String, itemId: Long): CartDto {
        val client = findClient.find(phoneNumber, key)
        val cart = cartRepository.findByClient(client) ?: throw CartNotFoundException()
        val cartItem = cartItemRepository.findByIdAndCart(itemId, cart) ?: throw CartItemNotFoundException()
        cartItemRepository.delete(cartItem)
        if (cartItemRepository.findByCart(cart).count() == 0)
            cartRepository.delete(cart)

        return getCartContents.contents(phoneNumber, key)
    }
}

@ResponseStatus(HttpStatus.NOT_FOUND)
class CartItemNotFoundException : RuntimeException()

@ResponseStatus(HttpStatus.NOT_FOUND)
class CartNotFoundException : RuntimeException()
