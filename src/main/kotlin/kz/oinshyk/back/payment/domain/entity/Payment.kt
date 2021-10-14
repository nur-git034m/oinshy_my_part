package kz.oinshyk.back.payment.domain.entity

import kz.oinshyk.back.client.domain.entity.Client
import kz.oinshyk.back.common.domain.entity.BaseEntity
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.persistence.*
import javax.validation.constraints.FutureOrPresent
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@Entity
class Payment(

        @NotNull
        @Enumerated(EnumType.STRING)
        var provider: PaymentsProvider,

        @NotNull
        @ManyToOne
        var client: Client,

        @NotNull
        @Enumerated(EnumType.STRING)
        var paymentFor: PaymentFor,

        @NotNull
        @Positive
        @Column(scale = 2)
        var amount: BigDecimal,

        @NotNull
        var successful: Boolean = false,

        @NotNull
        @FutureOrPresent
        var createAt: LocalDateTime = LocalDateTime.now(),

        var providerRef: String? = null

) : BaseEntity()

enum class PaymentFor {
    Subscription, Toys
}

enum class PaymentsProvider {
    CloudPayments
}
