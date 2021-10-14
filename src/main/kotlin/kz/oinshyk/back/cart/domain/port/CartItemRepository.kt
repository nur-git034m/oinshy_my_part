package kz.oinshyk.back.cart.domain.port

import kz.oinshyk.back.cart.domain.entity.Cart
import kz.oinshyk.back.cart.domain.entity.CartItem
import org.springframework.data.repository.CrudRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource(exported = false)
interface CartItemRepository : CrudRepository<CartItem, Long> {

    fun findByCart(cart: Cart): Iterable<CartItem>

    fun findByIdAndCart(id: Long, cart: Cart): CartItem?
}
