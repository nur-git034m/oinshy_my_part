package kz.oinshyk.back.cart.domain.entity

import kz.oinshyk.back.catalog.domain.entity.Toy
import kz.oinshyk.back.common.domain.entity.BaseEntity
import java.math.BigDecimal
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.ManyToOne
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@Entity
class CartItem(

        @NotNull
        @ManyToOne
        var cart: Cart,

        @NotNull
        @ManyToOne
        var toy: Toy,

        @Positive
        @NotNull
        @Column(scale = 2)
        var price: BigDecimal,

        @NotNull
        @Positive
        var quantity: Int

) : BaseEntity()
