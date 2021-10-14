package kz.oinshyk.back.client.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import io.mockk.verifySequence
import kz.oinshyk.back.AppConfig
import kz.oinshyk.back.client.app.web.dto.ClientDto
import kz.oinshyk.back.client.app.web.dto.ClientValidationDto
import kz.oinshyk.back.client.domain.entity.Client
import kz.oinshyk.back.client.domain.port.ChildRepository
import kz.oinshyk.back.client.domain.port.ClientRepository
import kz.oinshyk.back.security.domain.port.RandomGenerator
import kz.oinshyk.back.sms.domain.port.SmsProvider
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class RegisterClientTest {

    @MockK
    lateinit var clientRepository: ClientRepository

    @MockK
    lateinit var childRepository: ChildRepository

    @MockK
    lateinit var smsProvider: SmsProvider

    @MockK
    lateinit var randomGenerator: RandomGenerator

    @MockK
    lateinit var appConfig: AppConfig

    @Test
    fun `Number already registered`() {
        every { clientRepository.findByPhoneNumber("0123") } returns Client("0123", 0, "")

        val registerClient = RegisterClient(clientRepository, childRepository, smsProvider, randomGenerator, appConfig)

        assertThrows<ClientAlreadyRegisteredException> {
            registerClient.register(ClientDto("0123", 0, "", emptyList()))
        }

        verify { clientRepository.findByPhoneNumber("0123") }

        confirmVerified(clientRepository, childRepository, smsProvider, randomGenerator, appConfig)
    }

    @Test
    fun `Login with a real phone number`() {
        every { appConfig.testPhoneNumber } returns "000123"
        every { clientRepository.findByPhoneNumber("0123") } returns Client("0123", 0, "")
        every { randomGenerator.generateString(128) } returns "xyz"
        every { randomGenerator.generateIntString(4) } returns "1234"
        every {
            clientRepository.save(Client("0123", 0, "", "xyz", "1234"))
        } returns Client("0123", 0, "", "xyz", "1234")
        every { smsProvider.sendPin("0123", "1234") } returns Unit

        val registerClient = RegisterClient(clientRepository, childRepository, smsProvider, randomGenerator, appConfig)

        registerClient.login(ClientDto("0123", 0, "", emptyList()))

        verifySequence {
            appConfig.testPhoneNumber
            clientRepository.findByPhoneNumber("0123")
            randomGenerator.generateString(128)
            randomGenerator.generateIntString(4)
            clientRepository.save(Client("0123", 0, "", "xyz", "1234"))
            smsProvider.sendPin("0123", "1234")
        }

        confirmVerified(clientRepository, childRepository, smsProvider, randomGenerator, appConfig)
    }

    @Test
    fun `Login with a test phone number`() {
        every { appConfig.testPhoneNumber } returns "000123"

        val registerClient = RegisterClient(clientRepository, childRepository, smsProvider, randomGenerator, appConfig)

        registerClient.login(ClientDto("000123", 0, "", emptyList()))

        verify { appConfig.testPhoneNumber }

        confirmVerified(clientRepository, childRepository, smsProvider, randomGenerator, appConfig)
    }

    @Test
    fun `Validate a test PIN - incorrect`() {
        every { clientRepository.findByPhoneNumber("000123") } returns Client("000123", 0, "", key = "key")
        every { appConfig.testPhoneNumber } returns "000123"
        every { appConfig.testPin } returns "1234"

        val registerClient = RegisterClient(clientRepository, childRepository, smsProvider, randomGenerator, appConfig)

        assertThrows<ClientValidationException> {
            registerClient.validate(ClientValidationDto("000123", "1230"))
        }

        verifySequence {
            clientRepository.findByPhoneNumber("000123")
            appConfig.testPhoneNumber
            appConfig.testPin
        }

        confirmVerified(clientRepository, childRepository, smsProvider, randomGenerator, appConfig)
    }

    @Test
    fun `Validate a test PIN - correct`() {
        every { clientRepository.findByPhoneNumber("000123") } returns Client("000123", 0, "", key = "key")
        every { appConfig.testPhoneNumber } returns "000123"
        every { appConfig.testPin } returns "1234"

        val registerClient = RegisterClient(clientRepository, childRepository, smsProvider, randomGenerator, appConfig)

        Assertions.assertThat(
            registerClient.validate(ClientValidationDto("000123", "1234"))
        ).isEqualTo("key")

        verifySequence {
            clientRepository.findByPhoneNumber("000123")
            appConfig.testPhoneNumber
            appConfig.testPin
        }

        confirmVerified(clientRepository, childRepository, smsProvider, randomGenerator, appConfig)
    }
}
