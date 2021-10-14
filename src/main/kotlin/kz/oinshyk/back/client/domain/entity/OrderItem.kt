package kz.oinshyk.back.client.domain.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import kz.oinshyk.back.catalog.domain.entity.Toy
import kz.oinshyk.back.common.domain.entity.BaseEntity
import java.math.BigDecimal
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.ManyToOne
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@Entity
data class OrderItem(

    @get:JsonIgnore
    @NotNull
    @ManyToOne
    var order: ClientOrder,

    @NotNull
    @ManyToOne
    var toy: Toy,

    @Positive
    @NotNull
    @Column(scale = 2)
    var price: BigDecimal,

    @Positive
    @NotNull
    @Column(scale = 2)
    var originalPrice: BigDecimal,

    @Positive
    @NotNull
    var quantity: Int

) : BaseEntity()
