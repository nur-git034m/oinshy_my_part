package kz.oinshyk.back.dictionary.domain.entity

import kz.oinshyk.back.common.domain.entity.BaseEntity
import org.hibernate.annotations.NaturalId
import javax.persistence.Entity
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
class Setting(
        @NotNull
        @NotBlank
        @NaturalId(mutable = true)
        var key: String,

        @NotNull
        @NotBlank
        var value: String
) : BaseEntity()
