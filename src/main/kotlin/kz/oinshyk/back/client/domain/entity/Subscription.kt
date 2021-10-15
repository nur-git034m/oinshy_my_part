package kz.oinshyk.back.client.domain.entity

import kz.oinshyk.back.common.domain.entity.BaseEntity
import kz.oinshyk.back.payment.domain.entity.Payment
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.ManyToOne
import javax.validation.constraints.Future
import javax.validation.constraints.NotNull

@Entity
class Subscription(

        @NotNull
        @ManyToOne
        var client: Client,

        @NotNull
        @Future
        var validUntil: LocalDateTime,

        @NotNull
        @ManyToOne
        var payment: Payment

) : BaseEntity()
