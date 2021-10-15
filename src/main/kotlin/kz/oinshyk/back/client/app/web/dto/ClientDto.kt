package kz.oinshyk.back.client.app.web.dto

import kz.oinshyk.back.client.domain.entity.Gender
import java.time.LocalDate
import javax.validation.constraints.PastOrPresent
import javax.validation.constraints.Pattern
import javax.validation.constraints.PositiveOrZero

data class ClientDto(
    @get:Pattern(regexp = """\d{11,15}""")
    val phoneNumber: String,

    @get:PositiveOrZero
    val children: Int,

    val name: String,

    val childrenInfo: List<ChildDto>
)

data class ChildDto(
    val gender: Gender,

    @get:PastOrPresent
    val birthDate: LocalDate
)
