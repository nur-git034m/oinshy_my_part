package kz.oinshyk.back.catalog.domain.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import kz.oinshyk.back.common.domain.entity.BaseEntity
import javax.persistence.Entity
import javax.persistence.ManyToOne
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
class ToyImage(

        @NotNull
        @NotBlank
        var fileName: String,

        @get:JsonIgnore
        @NotNull
        @ManyToOne
        var toy: Toy

) : BaseEntity()
