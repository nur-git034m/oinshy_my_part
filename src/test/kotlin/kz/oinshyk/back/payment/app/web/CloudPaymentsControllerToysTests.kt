package kz.oinshyk.back.payment.app.web

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import kz.oinshyk.back.BaseMvcIntegrationTest
import kz.oinshyk.back.client.domain.port.ClientOrderRepository
import kz.oinshyk.back.payment.domain.entity.PaymentFor
import kz.oinshyk.back.payment.domain.port.PaymentRepository
import kz.oinshyk.back.payment.infra.CloudPaymentsModel
import kz.oinshyk.back.payment.infra.CloudPaymentsResponseDto
import kz.oinshyk.back.security.infra.runWithRole
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureMockRestServiceServer
import org.springframework.http.MediaType
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.test.web.servlet.post
import java.math.BigDecimal

@AutoConfigureMockRestServiceServer
internal class CloudPaymentsControllerToysTests(
        private val mockServer: MockRestServiceServer,
        private val paymentRepository: PaymentRepository,
        private val orderRepository: ClientOrderRepository
) : BaseMvcIntegrationTest() {

    @Test
    @Sql("/scripts/payments.sql")
    fun `Buy toys - client not found`() {
        mvc.post("/cloud-payments/charge") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(CloudPaymentsFrontChargeDto(
                    "12345678909",
                    "123",
                    PaymentFor.Toys,
                    "123abc",
                    "JON DOW",
                    BigDecimal(1234),
                    "1.2.3.4",
                    1
            ))
        }.andExpect { status { isUnauthorized } }
    }

    @Test
    @Sql("/scripts/payments.sql")
    fun `Buy toys - invalid key`() {
        mvc.post("/cloud-payments/charge") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(CloudPaymentsFrontChargeDto(
                    "12345678901",
                    "123x",
                    PaymentFor.Toys,
                    "123abc",
                    "JON DOW",
                    BigDecimal(1234),
                    "1.2.3.4",
                    1
            ))
        }.andExpect { status { isUnauthorized } }
    }

    @Test
    @Sql("/scripts/payments.sql")
    fun `Buy toys - not found`() {
        mvc.post("/cloud-payments/charge") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(CloudPaymentsFrontChargeDto(
                    "12345678901",
                    "123",
                    PaymentFor.Toys,
                    "123abc",
                    "JON DOW",
                    BigDecimal(1234),
                    "1.2.3.4",
                    10
            ))
        }.andExpect { status { isPreconditionFailed } }
    }

    @Test
    @Sql("/scripts/payments.sql")
    fun `Buy toys - already paid`() {
        mvc.post("/cloud-payments/charge") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(CloudPaymentsFrontChargeDto(
                    "12345678901",
                    "123",
                    PaymentFor.Toys,
                    "123abc",
                    "JON DOW",
                    BigDecimal(1234),
                    "1.2.3.4",
                    4
            ))
        }.andExpect { status { isPreconditionFailed } }
    }

    @Test
    @Sql("/scripts/payments.sql", "/scripts/settings.sql")
    fun `Buy toys - inconsistent amount`() {
        mvc.post("/cloud-payments/charge") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(CloudPaymentsFrontChargeDto(
                    "12345678901",
                    "123",
                    PaymentFor.Toys,
                    "123abc",
                    "JON DOW",
                    BigDecimal(1234),
                    "1.2.3.4",
                    5
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
    @Sql("/scripts/payments.sql")
    fun `Buy toys - payment error`() {
        mockServer.expect(requestTo("https://api.cloudpayments.ru/payments/cards/charge"))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(CloudPaymentsResponseDto(false)),
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
                    PaymentFor.Toys,
                    "123abc",
                    "JON DOW",
                    BigDecimal(6900),
                    "1.2.3.4",
                    5
            ))
        }.andExpect {
            status { isOk }
            jsonPath("\$.success") { value(false) }
        }

        runWithRole("ADMIN") {
            Assertions.assertThat(paymentRepository.count()).isEqualTo(1)
            Assertions.assertThat(paymentRepository.findAll().first().successful).isEqualTo(false)
        }
    }

    @Test
    @Sql("/scripts/payments.sql")
    fun `Buy toys`() {
        mockServer.expect(requestTo("https://api.cloudpayments.ru/payments/cards/charge"))
                .andRespond(withSuccess(
                        ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE)
                                .writeValueAsString(CloudPaymentsResponseDto(true)),
                        MediaType.APPLICATION_JSON
                ))

        runWithRole("ADMIN") {
            Assertions.assertThat(paymentRepository.count()).isEqualTo(0)
            Assertions.assertThat(orderRepository.findById(5).get().paid).isEqualTo(false)
        }

        mvc.post("/cloud-payments/charge") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(CloudPaymentsFrontChargeDto(
                    "12345678901",
                    "123",
                    PaymentFor.Toys,
                    "123abc",
                    "JON DOW",
                    BigDecimal(6900),
                    "1.2.3.4",
                    5
            ))
        }.andExpect {
            status { isOk }
            jsonPath("\$.success") { value(true) }
        }

        runWithRole("ADMIN") {
            Assertions.assertThat(paymentRepository.count()).isEqualTo(1)
            Assertions.assertThat(paymentRepository.findAll().first().successful).isEqualTo(true)
            Assertions.assertThat(paymentRepository.findAll().first().amount).isEqualTo(BigDecimal(6900))
            Assertions.assertThat(orderRepository.findById(5).get().paid).isEqualTo(true)
        }
    }

    @Test
    @Sql("/scripts/payments.sql")
    fun `Buy toys 3DS - invalid phone number`() {
        mvc.post("/cloud-payments/post3ds") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(CloudPaymentsFrontPost3dsDto(
                    "12345678909",
                    "123",
                    PaymentFor.Toys,
                    123,
                    "xxx",
                    5
            ))
        }.andExpect { status { isUnauthorized } }
    }

    @Test
    @Sql("/scripts/payments.sql")
    fun `Buy toys 3DS - invalid key`() {
        mvc.post("/cloud-payments/post3ds") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(CloudPaymentsFrontPost3dsDto(
                    "12345678901",
                    "123x",
                    PaymentFor.Toys,
                    123,
                    "xxx",
                    5
            ))
        }.andExpect { status { isUnauthorized } }
    }

    @Test
    @Sql("/scripts/payments.sql", "/scripts/settings.sql")
    fun `Buy toys 3DS - inconsistent txnId`() {
        mockServer.expect(requestTo("https://api.cloudpayments.ru/payments/cards/charge"))
                .andRespond(withSuccess(
                        ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE)
                                .writeValueAsString(CloudPaymentsResponseDto(
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
                    PaymentFor.Toys,
                    "123abc",
                    "JON DOW",
                    BigDecimal(6900),
                    "1.2.3.4",
                    5
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
                    PaymentFor.Toys,
                    123,
                    "xxx",
                    5
            ))
        }.andExpect { status { isPreconditionFailed } }
    }

    @Test
    @Sql("/scripts/payments.sql", "/scripts/settings.sql")
    fun `Buy toys 3DS - payment is unauthorized`() {
        mockServer.expect(requestTo("https://api.cloudpayments.ru/payments/cards/charge"))
                .andRespond(withSuccess(
                        ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE)
                                .writeValueAsString(CloudPaymentsResponseDto(
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
                    PaymentFor.Toys,
                    "123abc",
                    "JON DOW",
                    BigDecimal(6900),
                    "1.2.3.4",
                    5
            ))
        }.andExpect {
            status { isOk }
            jsonPath("\$.success") { value(false) }
        }

        runWithRole("ADMIN") {
            Assertions.assertThat(paymentRepository.count()).isEqualTo(1)
            val payment = paymentRepository.findAll().first()!!
            Assertions.assertThat(payment.successful).isEqualTo(false)
            Assertions.assertThat(payment.amount).isEqualTo(BigDecimal(6900))
            Assertions.assertThat(payment.providerRef).isEqualTo("111")
        }

        mvc.post("/cloud-payments/post3ds") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(CloudPaymentsFrontPost3dsDto(
                    "12345678901",
                    "123",
                    PaymentFor.Toys,
                    111,
                    "xxx",
                    5
            ))
        }.andExpect {
            status { isOk }
            jsonPath("\$.success") { value(false) }
        }

        runWithRole("ADMIN") {
            Assertions.assertThat(paymentRepository.count()).isEqualTo(1)
            Assertions.assertThat(paymentRepository.findAll().first().successful).isEqualTo(false)
        }
    }

    @Test
    @Sql("/scripts/payments.sql", "/scripts/settings.sql")
    fun `Buy toys 3DS`() {
        mockServer.expect(requestTo("https://api.cloudpayments.ru/payments/cards/charge"))
                .andRespond(withSuccess(
                        ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE)
                                .writeValueAsString(CloudPaymentsResponseDto(
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
            Assertions.assertThat(orderRepository.findById(5).get().paid).isEqualTo(false)
        }

        mvc.post("/cloud-payments/charge") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(CloudPaymentsFrontChargeDto(
                    "12345678901",
                    "123",
                    PaymentFor.Toys,
                    "123abc",
                    "JON DOW",
                    BigDecimal(6900),
                    "1.2.3.4",
                    5
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
                    PaymentFor.Toys,
                    111,
                    "xxx",
                    5
            ))
        }.andExpect {
            status { isOk }
            jsonPath("\$.success") { value(true) }
        }

        runWithRole("ADMIN") {
            Assertions.assertThat(paymentRepository.count()).isEqualTo(1)
            Assertions.assertThat(paymentRepository.findAll().first().successful).isEqualTo(true)
            Assertions.assertThat(paymentRepository.findAll().first().amount).isEqualTo(BigDecimal(6900))
            Assertions.assertThat(orderRepository.findById(5).get().paid).isEqualTo(true)
        }
    }
}
