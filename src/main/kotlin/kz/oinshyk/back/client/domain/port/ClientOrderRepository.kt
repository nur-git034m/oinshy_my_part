package kz.oinshyk.back.client.domain.port

import kz.oinshyk.back.client.domain.entity.Client
import kz.oinshyk.back.client.domain.entity.ClientOrder
import kz.oinshyk.back.client.domain.entity.ClientOrderStatus
import org.springframework.data.repository.CrudRepository
import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize("hasRole('ADMIN')")
interface ClientOrderRepository : CrudRepository<ClientOrder, Long> {

    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM')")
    override fun <S : ClientOrder?> save(entity: S): S

    @PreAuthorize("hasRole('SYSTEM')")
    fun findByIdAndClientAndStatusAndPaidIsFalse(id: Long, client: Client, status: ClientOrderStatus): ClientOrder?

    fun findByClientOrderByOrderedAtDesc(client: Client): Set<ClientOrder>

    fun findByStatusInOrderByOrderedAtDesc(statuses: Set<ClientOrderStatus>): Iterable<ClientOrder>

    @PreAuthorize("hasRole('SYSTEM')")
    fun findByClientAndStatusNot(client: Client, status: ClientOrderStatus): Iterable<ClientOrder>
}
