package kz.oinshyk.back.cart.app.web.dto

import kz.oinshyk.back.common.domain.entity.BaseClientDto
import javax.validation.constraints.Positive

class UpdateCartItemQuantityDto(
        phoneNumber: String,
        key: String,
        @get:Positive
        val quantity: Int
) : BaseClientDto(phoneNumber, key)
