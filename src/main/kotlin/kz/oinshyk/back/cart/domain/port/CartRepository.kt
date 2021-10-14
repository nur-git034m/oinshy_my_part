package kz.oinshyk.back.cart.domain.port

import kz.oinshyk.back.cart.domain.entity.Cart
import kz.oinshyk.back.client.domain.entity.Client
import org.springframework.data.repository.CrudRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource(exported = false)
interface CartRepository : CrudRepository<Cart, Long> {

    fun findByClient(client: Client): Cart?

    fun deleteByClient(client: Client)
}
