package kz.oinshyk.back.client.app.web

import kz.oinshyk.back.client.domain.entity.ClientOrderStatus
import kz.oinshyk.back.client.domain.usecase.FindOrdersByStatuses
import kz.oinshyk.back.client.domain.usecase.GetOrders
import kz.oinshyk.back.common.app.ApiController
import org.springframework.data.rest.webmvc.BasePathAwareController
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

@ApiController("orders")
@BasePathAwareController
class OrdersController(
        private val findOrdersByStatuses: FindOrdersByStatuses,
        private val getOrders: GetOrders
) {

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun findByStatuses(@RequestParam statuses: Set<ClientOrderStatus>) = findOrdersByStatuses.find(statuses)

    @GetMapping("{phoneNumber}/{key}")
    fun clientOrders(@PathVariable phoneNumber: String, @PathVariable key: String) = getOrders.get(phoneNumber, key)
}
