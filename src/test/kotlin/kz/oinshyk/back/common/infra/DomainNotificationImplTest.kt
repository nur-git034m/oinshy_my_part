//package kz.toyville.back.common.infra
//
//import com.ninjasquad.springmockk.MockkBean
//import io.mockk.confirmVerified
//import io.mockk.every
//import io.mockk.verifySequence
//import kz.toyville.back.client.domain.entity.Client
//import kz.toyville.back.client.domain.entity.ClientOrder
//import kz.toyville.back.client.domain.entity.ClientOrderPaymentType
//import kz.toyville.back.client.domain.entity.ClientOrderType
//import kz.toyville.back.dictionary.domain.entity.City
//import org.junit.jupiter.api.Test
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.context.ApplicationEventPublisher
//import org.springframework.mail.javamail.JavaMailSender
//import org.springframework.mail.javamail.MimeMessageHelper
//import org.springframework.test.annotation.Commit
//import org.springframework.test.context.TestConstructor
//import org.springframework.transaction.annotation.Transactional
//import java.math.BigDecimal
//import java.time.LocalDateTime
//import java.util.*
//import javax.mail.Session
//import javax.mail.internet.MimeMessage
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
//@AutoConfigureTestDatabase
//@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
//internal class DomainNotificationImplTest(
//    private val config: NotificationConfig,
//    private val applicationEventPublisher: ApplicationEventPublisher
//) {
//    @MockkBean
//    lateinit var mailSender: JavaMailSender
//
//    @Test
//    @Transactional
//    @Commit
//    fun `Send an email notification on a new order`() {
//        val message = MimeMessage(Session.getInstance(Properties()))
//        MimeMessageHelper(message).apply {
//            setFrom(config.sender)
//            setTo(config.admins.toTypedArray())
//            setSubject("Новый заказ")
//            setText("Поступил новый заказ № 1.")
//        }
//
//        every { mailSender.createMimeMessage() } returns message
//        every { mailSender.send(message) } returns Unit
//
//        applicationEventPublisher.publishEvent(
//            ClientOrder(
//                Client("123", 0, "Jon"),
//                ClientOrderPaymentType.UponDelivery,
//                ClientOrderType.Online,
//                BigDecimal(1000),
//                City("Almaty"),
//                "street",
//                "building",
//                LocalDateTime.now()
//            ).apply { id = 1 }
//        )
//
//        Thread.sleep(100)
//
//        verifySequence {
//            mailSender.createMimeMessage()
//            mailSender.send(message)
//        }
//
//        confirmVerified(mailSender)
//    }
//}
