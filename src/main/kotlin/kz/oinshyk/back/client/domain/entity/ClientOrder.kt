package kz.oinshyk.back.client.domain.entity

import kz.oinshyk.back.common.domain.entity.BaseEntity
import kz.oinshyk.back.dictionary.domain.entity.City
import kz.oinshyk.back.payment.domain.entity.Payment
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.PastOrPresent
import javax.validation.constraints.PositiveOrZero

@Entity
data class ClientOrder(

    @NotNull
    @ManyToOne
    var client: Client,

    @NotNull
    @Enumerated(EnumType.STRING)
    var paymentType: ClientOrderPaymentType,

    @NotNull
    @Enumerated(EnumType.STRING)
    var type: ClientOrderType,

    @NotNull
    @PositiveOrZero
    @Column(scale = 2)
    var deliveryPrice: BigDecimal,

    @NotNull
    @ManyToOne
    var city: City,

    @NotNull
    @get:NotBlank
    var street: String,

    @NotNull
    @get:NotBlank
    var building: String,

    @get:PastOrPresent
    @NotNull
    var orderedAt: LocalDateTime,

    var apartment: String? = null,

    @NotNull
    @Enumerated(EnumType.STRING)
    var status: ClientOrderStatus = ClientOrderStatus.Ordered,

    @NotNull
    var paid: Boolean = false,

    @ManyToOne
    var payment: Payment? = null,

    @Column(length = 16384)
    var declineReason: String? = null

) : BaseEntity() {

    @Transient
    var items: MutableList<OrderItem> = mutableListOf()

    fun getTotalAmount() =
        items.map { it.price * BigDecimal(it.quantity) }.fold(BigDecimal.ZERO) { a, i -> a + i } + deliveryPrice
}

enum class ClientOrderStatus {
    Ordered,
    Cancelled,

    @Suppress("unused")
    OnDelivery,
    Delivered
}

enum class ClientOrderPaymentType {
    UponDelivery,

    @Suppress("unused")
    ByPaymentCard
}

enum class ClientOrderType {
    Online,

    @Suppress("unused")
    Offline
}
