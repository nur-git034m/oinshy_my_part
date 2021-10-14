package kz.oinshyk.back.cart.app.web.dto

import kz.oinshyk.back.common.domain.entity.BaseClientDto
import javax.validation.constraints.Positive

class AddToyToCartDto(
        phoneNumber: String,
        key: String,
        val toyId: Long,
        @get:Positive
        val quantity: Int = 1
) : BaseClientDto(phoneNumber, key)
