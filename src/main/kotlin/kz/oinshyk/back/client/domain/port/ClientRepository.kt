package kz.oinshyk.back.client.domain.port

import kz.oinshyk.back.client.domain.entity.Client
import org.springframework.data.repository.CrudRepository
import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM')")
interface ClientRepository : CrudRepository<Client, Long> {

    @PreAuthorize("hasRole('SYSTEM')")
    fun findByPhoneNumber(phoneNumber: String): Client?

    @PreAuthorize("hasRole('SYSTEM')")
    fun findByPhoneNumberAndKey(phoneNumber: String, key: String): Client?
}
