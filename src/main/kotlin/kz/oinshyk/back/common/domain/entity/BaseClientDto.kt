package kz.oinshyk.back.common.domain.entity

import javax.validation.constraints.NotBlank

open class BaseClientDto(
        @get:NotBlank
        val phoneNumber: String,
        @get:NotBlank
        val key: String
)
