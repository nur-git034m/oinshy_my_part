package kz.oinshyk.back.client.domain.usecase

import kz.oinshyk.back.client.domain.entity.Child
import kz.oinshyk.back.client.domain.entity.Client
import kz.oinshyk.back.client.domain.entity.ClientOrder
import kz.oinshyk.back.client.domain.port.ChildRepository
import kz.oinshyk.back.client.domain.port.ClientOrderRepository
import kz.oinshyk.back.client.domain.port.ClientRepository
import kz.oinshyk.back.client.domain.port.OrderItemRepository
import kz.oinshyk.back.client.domain.service.FindClient
import kz.oinshyk.back.client.domain.service.FindSubscription
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.ResponseStatus

@Service
class GetClientInfo(
    private val findClient: FindClient,
    private val clientRepository: ClientRepository,
    private val childRepository: ChildRepository,
    private val findSubscription: FindSubscription,
    private val orderRepository: ClientOrderRepository,
    private val orderItemRepository: OrderItemRepository
) {

    @PreAuthorize("hasRole('ADMIN')")
    fun get(id: Long): ClientInfoWithOrdersDto {
        val client = clientRepository.findById(id).orElseThrow { throw ClientInfoNotFoundException() }
        return ClientInfoWithOrdersDto(
            client,
            findSubscription.exists(client),
            orderRepository.findByClientOrderByOrderedAtDesc(client)
                .map {
                    it.items = orderItemRepository.findByOrder(it)
                    it
                }.toSet()
        )
    }

    @PreAuthorize("hasRole('ADMIN')")
    fun get(client: Client) = get(client.id!!)

    fun get(phoneNumber: String, key: String): ClientInfoDto {
        val client = findClient.find(phoneNumber, key)
        val childrenInfo = childRepository.findByClientOrderByBirthDate(client) ?: emptyList()
        return ClientInfoDto(
            client.id,
            client.name,
            client.children,
            childrenInfo
        )
    }

}

data class ClientInfoDto(
    val id: Long?,
    val name: String,
    val children: Int,
    val childrenInfo: List<Child>
)

@ResponseStatus(HttpStatus.NOT_FOUND)
class ClientInfoNotFoundException : RuntimeException()

data class ClientInfoWithOrdersDto(
    val client: Client,
    val hasSubscription: Boolean,
    val orders: Set<ClientOrder>
)
