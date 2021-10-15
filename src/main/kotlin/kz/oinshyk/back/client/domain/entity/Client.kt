package kz.oinshyk.back.client.domain.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import kz.oinshyk.back.common.domain.entity.BaseEntity
import org.hibernate.annotations.NaturalId
import javax.persistence.Entity
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.PositiveOrZero

@Entity
data class Client(

    @NaturalId
    @NotNull
    @get:Pattern(regexp = """\d{11,15}""")
    var phoneNumber: String,

    @NotNull
    @get:PositiveOrZero
    var children: Int,

    var name: String,

    @get:JsonIgnore
    var temporalKey: String? = null,

    @get:JsonIgnore
    var pin: String? = null,

    @get:JsonIgnore
    var key: String? = null

) : BaseEntity()
