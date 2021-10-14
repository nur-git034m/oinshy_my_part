package kz.oinshyk.back.cart.domain.usecase

import kz.oinshyk.back.cart.domain.entity.Cart
import kz.oinshyk.back.cart.domain.entity.CartItem
import kz.oinshyk.back.cart.domain.port.CartItemRepository
import kz.oinshyk.back.cart.domain.port.CartRepository
import kz.oinshyk.back.cart.app.web.dto.AddToyToCartDto
import kz.oinshyk.back.catalog.domain.port.ToyRepository
import kz.oinshyk.back.client.domain.service.FindClient
import kz.oinshyk.back.client.domain.service.FindSubscription
import kz.oinshyk.back.client.domain.service.SubscriptionNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.ResponseStatus

@Service
class AddToyToCart(
        private val findClient: FindClient,
        private val findSubscription: FindSubscription,
        private val getCartContents: GetCartContents,
        private val toyRepository: ToyRepository,
        private val cartRepository: CartRepository,
        private val cartItemRepository: CartItemRepository
) {

    @Transactional
    fun add(dto: AddToyToCartDto): CartDto {
        val client = findClient.find(dto.phoneNumber, dto.key)
        val toy = toyRepository.findById(dto.toyId).orElseThrow { throw ToyNotFoundException() }
        val cart = cartRepository.findByClient(client) ?: cartRepository.save(Cart(client))
        val price = try {
            findSubscription.find(client)
            toy.subscriptionPrice
        } catch (e: SubscriptionNotFoundException) {
            toy.price
        }
        cartItemRepository.save(CartItem(cart, toy, price, dto.quantity))

        return getCartContents.contents(dto.phoneNumber, dto.key)
    }
}

@ResponseStatus(HttpStatus.NOT_FOUND)
class ToyNotFoundException : java.lang.RuntimeException()
