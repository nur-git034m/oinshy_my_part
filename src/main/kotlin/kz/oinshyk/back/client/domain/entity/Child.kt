package kz.oinshyk.back.client.domain.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import kz.oinshyk.back.common.domain.entity.BaseEntity
import java.time.LocalDate
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.ManyToOne
import javax.validation.constraints.NotNull
import javax.validation.constraints.PastOrPresent

@Entity
data class Child(
    @get:JsonIgnore
    @NotNull
    @ManyToOne
    var client: Client,

    @NotNull
    @Enumerated(EnumType.STRING)
    var gender: Gender,

    @NotNull
    @PastOrPresent
    var birthDate: LocalDate

) : BaseEntity()

enum class Gender {
    Male, Female,
}
