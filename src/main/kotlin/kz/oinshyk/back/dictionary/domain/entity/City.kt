package kz.oinshyk.back.dictionary.domain.entity

import kz.oinshyk.back.common.domain.entity.BaseEntity
import javax.persistence.Entity
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
data class City(
        @NotNull
        @NotBlank
        var name: String
) : BaseEntity()
