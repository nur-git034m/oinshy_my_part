package kz.oinshyk.back.payment.app.web

import kz.oinshyk.back.common.app.ApiController
import kz.oinshyk.back.common.domain.entity.BaseClientDto
import kz.oinshyk.back.payment.domain.entity.PaymentFor
import kz.oinshyk.back.payment.infra.CloudPayments
import org.springframework.data.rest.webmvc.BasePathAwareController
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import java.math.BigDecimal
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Positive

@ApiController("cloud-payments")
@BasePathAwareController
class CloudPaymentsController(private val cloudPayments: CloudPayments) {

    @PostMapping("charge")
    fun charge(@Valid @RequestBody dto: CloudPaymentsFrontChargeDto) = cloudPayments.charge(dto)

    @PostMapping("post3ds")
    fun post3ds(@Valid @RequestBody dto: CloudPaymentsFrontPost3dsDto) = cloudPayments.post3ds(dto)
}

class CloudPaymentsFrontChargeDto(
        phoneNumber: String,
        key: String,
        val paymentFor: PaymentFor,
        @get:NotBlank
        val cryptogram: String,
        val name: String,
        @get:Positive
        val amount: BigDecimal,
        val ipAddress: String,
        val orderId: Long? = null
) : BaseClientDto(phoneNumber, key)

class CloudPaymentsFrontPost3dsDto(
        phoneNumber: String,
        key: String,
        val paymentFor: PaymentFor,
        val transactionId: Long,
        val paRes: String,
        val orderId: Long? = null
) : BaseClientDto(phoneNumber, key)
