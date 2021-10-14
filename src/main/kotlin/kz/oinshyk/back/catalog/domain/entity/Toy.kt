package kz.oinshyk.back.catalog.domain.entity

import kz.oinshyk.back.common.domain.entity.BaseEntity
import org.hibernate.annotations.NaturalId
import java.math.BigDecimal
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.PositiveOrZero

@Entity
data class Toy(

    @NotNull
    @NotBlank
    @NaturalId(mutable = true)
    var sku: String,

    @NotNull
    @NotBlank
    var name: String,

    @Positive
    @NotNull
    @Column(scale = 2)
    var price: BigDecimal,

    @Positive
    @NotNull
    @Column(scale = 2)
    var subscriptionPrice: BigDecimal,

    @NotNull
    @PositiveOrZero
    var quantity: Int,

    @ManyToOne
    var category: Category?,

    @Column(length = 16384)
    var description: String? = null,

    @NotNull
    var show: Boolean = true,

    @NotNull
    var showOnMainPage: Boolean = false,

    @OneToMany(cascade = [CascadeType.ALL], mappedBy = "toy", fetch = FetchType.EAGER)
    var images: MutableList<ToyImage> = mutableListOf()

) : BaseEntity() {
    val hasCategory: Boolean get() = category != null
}
