package kz.oinshyk.back.client.app.web

import kz.oinshyk.back.client.app.web.dto.OrderDto
import kz.oinshyk.back.client.domain.entity.ClientOrderStatus
import kz.oinshyk.back.client.domain.usecase.CancelOrder
import kz.oinshyk.back.client.domain.usecase.ChangeStatus
import kz.oinshyk.back.client.domain.usecase.GetOrderInfo
import kz.oinshyk.back.client.domain.usecase.NewOrder
import kz.oinshyk.back.common.app.ApiController
import kz.oinshyk.back.common.domain.entity.BaseClientDto
import org.springframework.data.rest.webmvc.BasePathAwareController
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@ApiController("order")
@BasePathAwareController
class OrderController(
        private val newOrder: NewOrder,
        private val cancelOrder: CancelOrder,
        private val getOrderInfo: GetOrderInfo,
        private val changeStatus: ChangeStatus
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun newOrder(@Valid @RequestBody dto: OrderDto) = newOrder.order(dto)

    @PatchMapping("{id}/cancel")
    fun cancel(@Valid @RequestBody dto: BaseClientDto, @PathVariable id: Long) = cancelOrder.cancel(dto, id)

    @GetMapping("admin-info/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun getAdminInfo(@PathVariable id: Long) = getOrderInfo.get(id)

    @PatchMapping("{id}/set-status")
    @PreAuthorize("hasRole('ADMIN')")
    fun setStatusByAdmin(@PathVariable id: Long, @Valid @RequestBody dto: ChangeStatusDto) = changeStatus.update(id, dto)
}

data class ChangeStatusDto(
        val status: ClientOrderStatus,
        val declineReason: String?,
        val paid: Boolean
)
