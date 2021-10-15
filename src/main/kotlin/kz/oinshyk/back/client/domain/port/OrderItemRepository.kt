package kz.oinshyk.back.client.domain.port

import kz.oinshyk.back.client.domain.entity.ClientOrder
import kz.oinshyk.back.client.domain.entity.OrderItem
import org.springframework.data.repository.CrudRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource(exported = false)
interface OrderItemRepository : CrudRepository<OrderItem, Long> {

    fun findByOrder(order: ClientOrder): MutableList<OrderItem>

}
