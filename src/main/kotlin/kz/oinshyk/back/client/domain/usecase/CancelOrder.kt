package kz.oinshyk.back.client.domain.usecase

import kz.oinshyk.back.client.domain.entity.ClientOrderStatus
import kz.oinshyk.back.client.domain.port.ClientOrderRepository
import kz.oinshyk.back.client.domain.service.FindClient
import kz.oinshyk.back.common.domain.entity.BaseClientDto
import kz.oinshyk.back.security.infra.runWithRole
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.ResponseStatus

@Service
class CancelOrder(
        private val findClient: FindClient,
        private val clientOrderRepository: ClientOrderRepository
) {

    fun cancel(dto: BaseClientDto, id: Long) {
        val client = findClient.find(dto.phoneNumber, dto.key)
        runWithRole {
            val order = clientOrderRepository.findByIdAndClientAndStatusAndPaidIsFalse(id, client, ClientOrderStatus.Ordered)
                    ?: throw ClientOrderNotFound()
            order.status = ClientOrderStatus.Cancelled
            clientOrderRepository.save(order)
        }
    }
}

@ResponseStatus(HttpStatus.NOT_FOUND)
class ClientOrderNotFound : RuntimeException()
