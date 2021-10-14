package kz.oinshyk.back.payment.domain.port

import kz.oinshyk.back.client.domain.entity.Client
import kz.oinshyk.back.payment.domain.entity.Payment
import kz.oinshyk.back.payment.domain.entity.PaymentsProvider
import org.springframework.data.repository.CrudRepository
import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize("hasRole('ADMIN')")
interface PaymentRepository : CrudRepository<Payment, Long> {

    @PreAuthorize("hasRole('SYSTEM')")
    fun findByClientAndProviderAndProviderRefAndSuccessfulIsFalse(client: Client, provider: PaymentsProvider, providerRef: String): Payment?
}
