package kz.oinshyk.back.cart.domain.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import kz.oinshyk.back.client.domain.entity.Client
import kz.oinshyk.back.common.domain.entity.BaseEntity
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.validation.constraints.NotNull

// TODO: maybe there's a necessity to clear old carts after some days

@Entity
class Cart(

        @NotNull
        @ManyToOne
        var client: Client,

        @get:JsonIgnore
        @OneToMany(cascade = [CascadeType.REMOVE], mappedBy = "cart")
        var items: MutableList<CartItem> = mutableListOf()

) : BaseEntity()
