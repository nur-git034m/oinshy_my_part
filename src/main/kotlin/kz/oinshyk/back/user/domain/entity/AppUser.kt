package kz.oinshyk.back.user.domain.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import kz.oinshyk.back.common.domain.entity.BaseEntity
import org.hibernate.annotations.NaturalId
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
class AppUser(

    @NotBlank
    @NaturalId(mutable = true)
    @Email
    var login: String,

    @get:JsonIgnore
    var password: String,

    @NotNull
    @Enumerated(EnumType.STRING)
    var role: AppUserRole

) : BaseEntity()

enum class AppUserRole {
    ADMIN, EXCHANGER
}
