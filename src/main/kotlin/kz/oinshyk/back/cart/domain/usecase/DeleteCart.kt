package kz.oinshyk.back.cart.domain.usecase

import kz.oinshyk.back.cart.domain.port.CartRepository
import kz.oinshyk.back.client.domain.service.FindClient
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DeleteCart(
        private val findClient: FindClient,
        private val cartRepository: CartRepository
) {

    @Transactional
    fun delete(phoneNumber: String, key: String) {
        val client = findClient.find(phoneNumber, key)
        cartRepository.deleteByClient(client)
    }
}
