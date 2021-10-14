package kz.oinshyk.back.client.app.web.dto

import kz.oinshyk.back.client.domain.entity.ClientOrderPaymentType
import kz.oinshyk.back.common.domain.entity.BaseClientDto
import kz.oinshyk.back.dictionary.domain.entity.City

class OrderDto(
        phoneNumber: String,
        key: String,
        val city: City,
        val street: String,
        val building: String,
        val apartment: String,
        val paymentType: ClientOrderPaymentType
) : BaseClientDto(phoneNumber, key)
