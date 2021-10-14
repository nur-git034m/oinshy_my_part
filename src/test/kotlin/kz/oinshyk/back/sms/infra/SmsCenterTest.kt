package kz.oinshyk.back.sms.infra

import com.fasterxml.jackson.databind.ObjectMapper
import kz.oinshyk.back.sms.infra.dto.SmsCenterResponseDto
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess

@RestClientTest(SmsCenter::class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@ActiveProfiles("test")
class SmsCenterTest(
        private val mockServer: MockRestServiceServer,
        private val smsCenter: SmsCenter,
        private val mapper: ObjectMapper
) {

    @BeforeEach
    fun setup() = mockServer.reset()

    @Test
    fun `Send PIN verification message`() {
        mockServer.expect(requestTo("https://smsc.kz/sys/send.php?login=test&psw=testpsw" +
                "&fmt=3&charset=utf-8&phones=12345678901&mes=ToyVille:%201234"))
                .andRespond(withSuccess("{\"id\": 777,\"cnt\": 1}", MediaType.APPLICATION_JSON))

        smsCenter.sendPin("12345678901", "1234")

        mockServer.verify()
    }

    @Test
    fun `Send PIN verification message - error`() {
        mockServer.expect(requestTo("https://smsc.kz/sys/send.php?login=test&psw=testpsw" +
                "&fmt=3&charset=utf-8&phones=12345678901&mes=ToyVille:%201234"))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(SmsCenterResponseDto("1", "Error", 1)),
                        MediaType.APPLICATION_JSON
                ))

        assertThrows<SmsCenterException> {
            smsCenter.sendPin("12345678901", "1234")
        }

        mockServer.verify()
    }
}
