package kz.oinshyk.back.payment.infra

import kz.oinshyk.back.client.domain.entity.Client
import kz.oinshyk.back.client.domain.entity.ClientOrderStatus
import kz.oinshyk.back.client.domain.entity.Subscription
import kz.oinshyk.back.client.domain.port.ClientOrderRepository
import kz.oinshyk.back.client.domain.port.OrderItemRepository
import kz.oinshyk.back.client.domain.port.SubscriptionRepository
import kz.oinshyk.back.client.domain.service.FindClient
import kz.oinshyk.back.client.domain.service.FindSubscription
import kz.oinshyk.back.dictionary.domain.service.GetSubscriptionPrice
import kz.oinshyk.back.dictionary.domain.service.SettingNotFound
import kz.oinshyk.back.payment.app.web.CloudPaymentsFrontChargeDto
import kz.oinshyk.back.payment.app.web.CloudPaymentsFrontPost3dsDto
import kz.oinshyk.back.payment.domain.entity.Payment
import kz.oinshyk.back.payment.domain.entity.PaymentFor
import kz.oinshyk.back.payment.domain.entity.PaymentsProvider
import kz.oinshyk.back.payment.domain.port.PaymentRepository
import kz.oinshyk.back.security.infra.runWithRole
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForObject
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class CloudPayments(
    private val config: CloudPaymentsConfig,
    private val findClient: FindClient,
    private val findSubscription: FindSubscription,
    private val getSubscriptionPrice: GetSubscriptionPrice,
    private val paymentRepository: PaymentRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val orderRepository: ClientOrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val restTemplate: RestTemplate
) {

    private val chargePath = "/payments/cards/charge"
    private val post3dsPath = "/payments/cards/post3ds"

    private val subscriptionDurationInMonths = 6L

    @Transactional
    fun charge(dto: CloudPaymentsFrontChargeDto): CloudPaymentsResponseDto {
        val client = findClient.find(dto.phoneNumber, dto.key)

        val payment = Payment(PaymentsProvider.CloudPayments, client, dto.paymentFor, dto.amount)
        runWithRole("ADMIN") {
            paymentRepository.save(payment)
        }

        when (dto.paymentFor) {
            PaymentFor.Subscription -> checkBuySubscription(client, payment)
            PaymentFor.Toys -> checkBuyToys(dto.orderId!!, client, payment)
        }

        val chargeDto =
            CloudPaymentsChargeDto(dto.amount, "KZT", dto.ipAddress, dto.name, dto.cryptogram, payment.id.toString())
        val entity = createEntity(payment, chargeDto)

        return process(
            ProcessingParams(
                dto.paymentFor,
                chargePath,
                client,
                payment,
                entity,
                dto.orderId
            )
        )
    }

    private fun process(params: ProcessingParams): CloudPaymentsResponseDto {
        val responseDto =
            restTemplate.postForObject<CloudPaymentsResponseDto>("${config.apiUrl}${params.path}", params.entity)

        if (responseDto.Success) {
            params.payment.successful = true
            params.payment.providerRef = responseDto.Model?.TransactionId.toString()
            runWithRole("ADMIN") {
                paymentRepository.save(params.payment)
            }

            when (params.paymentFor) {
                PaymentFor.Subscription -> subscriptionRepository.save(
                    Subscription(
                        params.client,
                        LocalDateTime.now().plusMonths(subscriptionDurationInMonths),
                        params.payment
                    )
                )
                PaymentFor.Toys -> {
                    runWithRole {
                        val order = orderRepository.findByIdAndClientAndStatusAndPaidIsFalse(
                            params.orderId!!,
                            params.client,
                            ClientOrderStatus.Ordered
                        ) ?: throw CloudPaymentsPreconditionFailedException()
                        order.paid = true
                        order.payment = params.payment
                        orderRepository.save(order)
                    }
                }
            }
        } else if (params.payment.providerRef == null) {
            params.payment.providerRef = responseDto.Model?.TransactionId.toString()
            runWithRole("ADMIN") {
                paymentRepository.save(params.payment)
            }
        }

        return responseDto
    }

    private fun createEntity(payment: Payment, dto: Any): HttpEntity<Any> {
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            setBasicAuth(config.publicId, config.apiSecret)
            set("X-Request-ID", payment.id.toString())
        }
        return HttpEntity(dto, headers)
    }

    private fun checkBuySubscription(client: Client, payment: Payment) {
        if (findSubscription.exists(client)) throw SubscriptionAlreadyExistsException()

        val price = try {
            getSubscriptionPrice.price()
        } catch (e: SettingNotFound) {
            throw CloudPaymentsPreconditionFailedException()
        }

        if (price != payment.amount.setScale(2)) throw InconsistentAmountException()
    }

    private fun checkBuyToys(orderId: Long, client: Client, payment: Payment) {
        val order = runWithRole {
            orderRepository.findByIdAndClientAndStatusAndPaidIsFalse(orderId, client, ClientOrderStatus.Ordered)
                ?: throw CloudPaymentsPreconditionFailedException()
        }

        order.apply { items = orderItemRepository.findByOrder(this) }

        if (order.getTotalAmount() != payment.amount.setScale(2)) throw InconsistentAmountException()
    }

    fun post3ds(dto: CloudPaymentsFrontPost3dsDto): CloudPaymentsResponseDto {
        val client = findClient.find(dto.phoneNumber, dto.key)

        val payment = runWithRole {
            paymentRepository.findByClientAndProviderAndProviderRefAndSuccessfulIsFalse(
                client,
                PaymentsProvider.CloudPayments,
                dto.transactionId.toString()
            ) ?: throw CloudPaymentsPreconditionFailedException()
        }

        val entity = createEntity(payment, CloudPaymentsPost3dsDto(dto.transactionId, dto.paRes))

        return process(
            ProcessingParams(
                dto.paymentFor,
                post3dsPath,
                client,
                payment,
                entity,
                dto.orderId
            )
        )
    }
}

data class CloudPaymentsChargeDto(
    val amount: BigDecimal,
    val currency: String,
    val ipAddress: String,
    val name: String,
    val cardCryptogramPacket: String,
    val invoiceId: String
)

data class CloudPaymentsResponseDto(
    val Success: Boolean,
    val Message: String? = null,
    val Model: CloudPaymentsModel? = null
)

data class CloudPaymentsModel(
    val TransactionId: Long,
    val PaReq: String?,
    val AcsUrl: String?,
    val CardHolderMessage: String?
//        val reasonCode: Long
)

data class CloudPaymentsPost3dsDto(
    val transactionId: Long,
    val paRes: String
)

internal data class ProcessingParams(
    val paymentFor: PaymentFor,
    val path: String,
    val client: Client,
    val payment: Payment,
    val entity: HttpEntity<Any>,
    val orderId: Long? = null
)

@ResponseStatus(HttpStatus.PRECONDITION_FAILED)
class SubscriptionAlreadyExistsException : RuntimeException()

@ResponseStatus(HttpStatus.PRECONDITION_FAILED)
class CloudPaymentsPreconditionFailedException : RuntimeException()

@ResponseStatus(HttpStatus.EXPECTATION_FAILED)
class InconsistentAmountException : RuntimeException()
