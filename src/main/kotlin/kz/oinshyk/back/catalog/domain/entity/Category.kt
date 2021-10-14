package kz.oinshyk.back.catalog.domain.entity

import kz.oinshyk.back.common.domain.entity.BaseEntity
import org.hibernate.annotations.NaturalId
import javax.persistence.Entity
import javax.persistence.ManyToOne
import javax.validation.constraints.NotBlank

@Entity
data class Category(

    @NotBlank
    var name: String,

    @NotBlank
    var image: String,

    @ManyToOne
    var parent: Category? = null,

    @NaturalId(mutable = true)
    var code: String? = null

) : BaseEntity() {
    val hasParent: Boolean get() = parent != null
}
