package kz.oinshyk.back.payment.app.web

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import kz.oinshyk.back.BaseMvcIntegrationTest
import kz.oinshyk.back.client.domain.port.SubscriptionRepository
import kz.oinshyk.back.payment.domain.entity.PaymentFor
import kz.oinshyk.back.payment.domain.port.PaymentRepository
import kz.oinshyk.back.payment.infra.CloudPaymentsModel
import kz.oinshyk.back.payment.infra.CloudPaymentsResponseDto
import kz.oinshyk.back.security.infra.runWithRole
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureMockRestServiceServer
import org.springframework.http.MediaType
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.test.web.servlet.post
import org.springframework.web.util.NestedServletException
import java.math.BigDecimal
import javax.validation.ConstraintViolationException

@AutoConfigureMockRestServiceServer
internal class CloudPaymentsControllerSubscriptionsTests(
        private val mockServer: MockRestServiceServer,
        private val paymentRepository: PaymentRepository,
        private val subscriptionRepository: SubscriptionRepository
) : BaseMvcIntegrationTest() {

    @Test
    fun `Buy subscription - empty phone number`() {
        val exception = assertThrows<NestedServletException> {
            mvc.post("/cloud-payments/charge") {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(CloudPaymentsFrontChargeDto(
                        "",
                        "123",
                        PaymentFor.Subscription,
                        "123abc",
                        "JON DOW",
                        BigDecimal(123),
                        "1.2.3.4"
                ))
            }
        }
        Assertions.assertThat(exception.cause).isInstanceOf(ConstraintViolationException::class.java)
        Assertions.assertThat(exception.cause?.message).isEqualTo("charge.dto.phoneNumber: must not be blank")
    }

    @Test
    fun `Buy subscription - empty cryptogram`() {
        val exception = assertThrows<NestedServletException> {
            mvc.post("/cloud-payments/charge") {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(CloudPaymentsFrontChargeDto(
                        "12345678901",
                        "123",
                        PaymentFor.Subscription,
                        "",
                        "JON DOW",
                        BigDecimal(123),
                        "1.2.3.4"
                ))
            }
        }
        Assertions.assertThat(exception.cause).isInstanceOf(ConstraintViolationException::class.java)
        Assertions.assertThat(exception.cause?.message).isEqualTo("charge.dto.cryptogram: must not be blank")
    }

    @Test
    fun `Buy subscription - zero amount`() {
        val exception = assertThrows<NestedServletException> {
            mvc.post("/cloud-payments/charge") {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(CloudPaymentsFrontChargeDto(
                        "12345678901",
                        "123",
                        PaymentFor.Subscription,
                        "123",
                        "JON DOW",
                        BigDecimal(0),
                        "1.2.3.4"
                ))
            }
        }
        Assertions.assertThat(exception.cause).isInstanceOf(ConstraintViolationException::class.java)
        Assertions.assertThat(exception.cause?.message).isEqualTo("charge.dto.amount: must be greater than 0")
    }

    @Test
    @Sql("/scripts/payments.sql")
    fun `Buy subscription - client not found`() {
        mvc.post("/cloud-payments/charge") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(CloudPaymentsFrontChargeDto(
                    "12345678909",
                    "123",
                    PaymentFor.Subscription,
                    "123abc",
                    "JON DOW",
                    BigDecimal(1234),
                    "1.2.3.4"
            ))
        }.andExpect { status { isUnauthorized } }
    }

    @Test
    @Sql("/scripts/payments.sql")
    fun `Buy subscription - invalid key`() {
        mvc.post("/cloud-payments/charge") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(CloudPaymentsFrontChargeDto(
                    "12345678901",
                    "123x",
                    PaymentFor.Subscription,
                    "123abc",
                    "JON DOW",
                    BigDecimal(1234),
                    "1.2.3.4"
            ))
        }.andExpect { status { isUnauthorized } }
    }

    @Test
    @Sql("/scripts/payments.sql")
    fun `Buy subscription - already has an active one`() {
        mvc.post("/cloud-payments/charge") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(CloudPaymentsFrontChargeDto(
                    "12345678902",
                    "123",
                    PaymentFor.Subscription,
                    "123abc",
                    "JON DOW",
                    BigDecimal(1234),
                    "1.2.3.4"
            ))
        }.andExpect { status { isPreconditionFailed } }
    }

    @Test
    @Sql("/scripts/payments.sql")
    fun `Buy subscription - subscription price not set`() {
        mvc.post("/cloud-payments/charge") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(CloudPaymentsFrontChargeDto(
                    "12345678901",
                    "123",
                    PaymentFor.Subscription,
                    "123abc",
                    "JON DOW",
                    BigDecimal(1234),
                    "1.2.3.4"
            ))
        }.andExpect { status { isPreconditionFailed } }
    }

    @Test
    @Sql("/scripts/payments.sql", "/scripts/settings.sql")
    fun `Buy subscription - inconsistent amount`() {
        mvc.post("/cloud-payments/charge") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(CloudPaymentsFrontChargeDto(
                    "12345678901",
                    "123",
                    PaymentFor.Subscription,
                    "123abc",
                    "JON DOW",
                    BigDecimal(1234),
                    "1.2.3.4"
            ))
        }.andExpect { status { isExpectationFailed } }
    }

    @BeforeEach
    fun setup() {
        mockServer.reset()
    }

    @AfterEach
    fun cleanup() {
        mockServer.verify()
    }

    @Test
    @Sql("/scripts/payments.sql", "/scripts/settings.sql")
    fun `Buy subscription - payment error`() {
        mockServer.expect(requestTo("https://api.cloudpayments.ru/payments/cards/charge"))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(CloudPaymentsResponseDto(false)),
                        MediaType.APPLICATION_JSON
                ))

        runWithRole("ADMIN") {
            Assertions.assertThat(paymentRepository.count()).isEqualTo(0)
        }
        Assertions.assertThat(subscriptionRepository.count()).isEqualTo(2)

        mvc.post("/cloud-payments/charge") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(CloudPaymentsFrontChargeDto(
                    "12345678901",
                    "123",
                    PaymentFor.Subscription,
                    "123abc",
                    "JON DOW",
                    BigDecimal(5000),
                    "1.2.3.4"
            ))
        }.andExpect {
            status { isOk }
            jsonPath("\$.success") { value(false) }
        }

        runWithRole("ADMIN") {
            Assertions.assertThat(paymentRepository.count()).isEqualTo(1)
            Assertions.assertThat(paymentRepository.findAll().first().successful).isEqualTo(false)
        }
        Assertions.assertThat(subscriptionRepository.count()).isEqualTo(2)
    }

    @Test
    @Sql("/scripts/payments.sql", "/scripts/settings.sql")
    fun `Buy subscription`() {
        mockServer.expect(requestTo("https://api.cloudpayments.ru/payments/cards/charge"))
                .andRespond(withSuccess(
                        ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE)
                                .writeValueAsString(CloudPaymentsResponseDto(true)),
                        MediaType.APPLICATION_JSON
                ))

        runWithRole("ADMIN") {
            Assertions.assertThat(paymentRepository.count()).isEqualTo(0)
        }
        Assertions.assertThat(subscriptionRepository.count()).isEqualTo(2)

        mvc.post("/cloud-payments/charge") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(CloudPaymentsFrontChargeDto(
                    "12345678901",
                    "123",
                    PaymentFor.Subscription,
                    "123abc",
                    "JON DOW",
                    BigDecimal(5000),
                    "1.2.3.4"
            ))
        }.andExpect {
            status { isOk }
            jsonPath("\$.success") { value(true) }
        }

        runWithRole("ADMIN") {
            Assertions.assertThat(paymentRepository.count()).isEqualTo(1)
            val payment = paymentRepository.findAll().first()!!
            Assertions.assertThat(payment.successful).isEqualTo(true)

            Assertions.assertThat(subscriptionRepository.count()).isEqualTo(3)
            Assertions.assertThat(subscriptionRepository.findByPayment(payment)).isNotNull
        }
    }

    @Test
    @Sql("/scripts/payments.sql", "/scripts/settings.sql")
    fun `Buy subscription 3DS - invalid phone number`() {
        mvc.post("/cloud-payments/post3ds") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(CloudPaymentsFrontPost3dsDto(
                    "12345678909",
                    "123",
                    PaymentFor.Subscription,
                    123,
                    "xxx"
            ))
        }.andExpect { status { isUnauthorized } }
    }

    @Test
    @Sql("/scripts/payments.sql", "/scripts/settings.sql")
    fun `Buy subscription 3DS - invalid key`() {
        mvc.post("/cloud-payments/post3ds") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(CloudPaymentsFrontPost3dsDto(
                    "12345678901",
                    "123x",
                    PaymentFor.Subscription,
                    123,
                    "xxx"
            ))
        }.andExpect { status { isUnauthorized } }
    }

    @Test
    @Sql("/scripts/payments.sql", "/scripts/settings.sql")
    fun `Buy subscription 3DS - inconsistent txnId`() {
        mockServer.expect(requestTo("https://api.cloudpayments.ru/payments/cards/charge"))
                .andRespond(withSuccess(
                        ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE)
                                .writeValueAsString(
                                        CloudPaymentsResponseDto(
                                                false,
                                                Model = CloudPaymentsModel(
                                                        111,
                                                        "req",
                                                        "url",
                                                        "msg"
                                                ))),
                        MediaType.APPLICATION_JSON
                ))

        runWithRole("ADMIN") {
            Assertions.assertThat(paymentRepository.count()).isEqualTo(0)
        }

        mvc.post("/cloud-payments/charge") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(CloudPaymentsFrontChargeDto(
                    "12345678901",
                    "123",
                    PaymentFor.Subscription,
                    "123abc",
                    "JON DOW",
                    BigDecimal(5000),
                    "1.2.3.4"
            ))
        }.andExpect {
            status { isOk }
            jsonPath("\$.success") { value(false) }
        }

        runWithRole("ADMIN") {
            Assertions.assertThat(paymentRepository.count()).isEqualTo(1)
            val payment = paymentRepository.findAll().first()!!
            Assertions.assertThat(payment.successful).isEqualTo(false)
            Assertions.assertThat(payment.providerRef).isEqualTo("111")
        }

        mvc.post("/cloud-payments/post3ds") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(CloudPaymentsFrontPost3dsDto(
                    "12345678901",
                    "123",
                    PaymentFor.Subscription,
                    123,
                    "xxx"
            ))
        }.andExpect { status { isPreconditionFailed } }
    }

    @Test
    @Sql("/scripts/payments.sql", "/scripts/settings.sql")
    fun `Buy subscription 3DS - payment is unauthorized`() {
        mockServer.expect(requestTo("https://api.cloudpayments.ru/payments/cards/charge"))
                .andRespond(withSuccess(
                        ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE)
                                .writeValueAsString(
                                        CloudPaymentsResponseDto(
                                                false,
                                                Model = CloudPaymentsModel(
                                                        111,
                                                        "req",
                                                        "url",
                                                        "msg"
                                                ))),
                        MediaType.APPLICATION_JSON
                ))

        mockServer.expect(requestTo("https://api.cloudpayments.ru/payments/cards/post3ds"))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(CloudPaymentsResponseDto(false)),
                        MediaType.APPLICATION_JSON
                ))

        mvc.post("/cloud-payments/charge") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(CloudPaymentsFrontChargeDto(
                    "12345678901",
                    "123",
                    PaymentFor.Subscription,
                    "123abc",
                    "JON DOW",
                    BigDecimal(5000),
                    "1.2.3.4"
            ))
        }.andExpect {
            status { isOk }
            jsonPath("\$.success") { value(false) }
        }

        runWithRole("ADMIN") {
            Assertions.assertThat(paymentRepository.count()).isEqualTo(1)
            val payment = paymentRepository.findAll().first()!!
            Assertions.assertThat(payment.successful).isEqualTo(false)
            Assertions.assertThat(payment.providerRef).isEqualTo("111")
        }

        mvc.post("/cloud-payments/post3ds") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(CloudPaymentsFrontPost3dsDto(
                    "12345678901",
                    "123",
                    PaymentFor.Subscription,
                    111,
                    "xxx"
            ))
        }.andExpect {
            status { isOk }
            jsonPath("\$.success") { value(false) }
        }

        runWithRole("ADMIN") {
            Assertions.assertThat(paymentRepository.count()).isEqualTo(1)
            Assertions.assertThat(paymentRepository.findAll().first().successful).isEqualTo(false)
        }
        Assertions.assertThat(subscriptionRepository.count()).isEqualTo(2)
    }

    @Test
    @Sql("/scripts/payments.sql", "/scripts/settings.sql")
    fun `Buy subscription 3DS`() {
        mockServer.expect(requestTo("https://api.cloudpayments.ru/payments/cards/charge"))
                .andRespond(withSuccess(
                        ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE)
                                .writeValueAsString(
                                        CloudPaymentsResponseDto(
                                                false,
                                                Model = CloudPaymentsModel(
                                                        111,
                                                        "req",
                                                        "url",
                                                        "msg"
                                                ))),
                        MediaType.APPLICATION_JSON
                ))

        mockServer.expect(requestTo("https://api.cloudpayments.ru/payments/cards/post3ds"))
                .andRespond(withSuccess(
                        ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE)
                                .writeValueAsString(CloudPaymentsResponseDto(true)),
                        MediaType.APPLICATION_JSON
                ))

        runWithRole("ADMIN") {
            Assertions.assertThat(paymentRepository.count()).isEqualTo(0)
        }
        Assertions.assertThat(subscriptionRepository.count()).isEqualTo(2)

        mvc.post("/cloud-payments/charge") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(CloudPaymentsFrontChargeDto(
                    "12345678901",
                    "123",
                    PaymentFor.Subscription,
                    "123abc",
                    "JON DOW",
                    BigDecimal(5000),
                    "1.2.3.4"
            ))
        }.andExpect {
            status { isOk }
            jsonPath("\$.success") { value(false) }
        }

        runWithRole("ADMIN") {
            Assertions.assertThat(paymentRepository.count()).isEqualTo(1)
            val payment = paymentRepository.findAll().first()!!
            Assertions.assertThat(payment.successful).isEqualTo(false)
            Assertions.assertThat(payment.providerRef).isEqualTo("111")
        }

        mvc.post("/cloud-payments/post3ds") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(CloudPaymentsFrontPost3dsDto(
                    "12345678901",
                    "123",
                    PaymentFor.Subscription,
                    111,
                    "xxx"
            ))
        }.andExpect {
            status { isOk }
            jsonPath("\$.success") { value(true) }
        }

        runWithRole("ADMIN") {
            Assertions.assertThat(paymentRepository.count()).isEqualTo(1)
            val payment = paymentRepository.findAll().first()!!
            Assertions.assertThat(payment.successful).isEqualTo(true)

            Assertions.assertThat(subscriptionRepository.count()).isEqualTo(3)
            Assertions.assertThat(subscriptionRepository.findByPayment(payment)).isNotNull
        }
    }
}
