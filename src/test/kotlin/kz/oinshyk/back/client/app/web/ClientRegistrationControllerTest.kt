package kz.oinshyk.back.client.app.web

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import kz.oinshyk.back.BaseMvcIntegrationTest
import kz.oinshyk.back.client.app.web.dto.ChildDto
import kz.oinshyk.back.client.app.web.dto.ClientDto
import kz.oinshyk.back.client.app.web.dto.ClientValidationDto
import kz.oinshyk.back.client.domain.entity.Child
import kz.oinshyk.back.client.domain.entity.Client
import kz.oinshyk.back.client.domain.entity.Gender
import kz.oinshyk.back.client.domain.port.ChildRepository
import kz.oinshyk.back.client.domain.port.ClientRepository
import kz.oinshyk.back.security.domain.port.RandomGenerator
import kz.oinshyk.back.security.infra.runWithRole
import kz.oinshyk.back.sms.domain.port.SmsProvider
import net.bytebuddy.utility.RandomString
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.MediaType
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import org.springframework.web.util.NestedServletException
import java.time.LocalDate
import javax.validation.ConstraintViolationException

internal class ClientRegistrationControllerTest(
    private val repository: ClientRepository,
    private val childRepository: ChildRepository
) : BaseMvcIntegrationTest() {

    @MockkBean
    private lateinit var smsProvider: SmsProvider

    @MockkBean
    private lateinit var randomGenerator: RandomGenerator

    @Test
    internal fun `Should fail - empty phone number`() {
        val exception = assertThrows<NestedServletException> {
            mvc.post("/clients/register") {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(
                    ClientDto(
                        "",
                        3,
                        "Jon",
                        childrenInfo = listOf(
                            ChildDto(
                                Gender.Male,
                                LocalDate.parse("2000-12-31")
                            ),
                            ChildDto(
                                Gender.Male,
                                LocalDate.parse("2001-12-31")
                            ),
                            ChildDto(
                                Gender.Female,
                                LocalDate.parse("2002-12-31")
                            )
                        )
                    )
                )
            }
        }
        assertThat(exception.cause).isInstanceOf(ConstraintViolationException::class.java)
        assertThat(exception.cause?.message).isEqualTo("register.clientDto.phoneNumber: must match \"\\d{11,15}\"")
    }

    @Test
    fun `Should fail - short phone number`() {
        val exception = assertThrows<NestedServletException> {
            mvc.post("/clients/register") {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(
                    ClientDto(
                        "1234567890", 3, "Jon",
                        childrenInfo = listOf(
                            ChildDto(
                                Gender.Male,
                                LocalDate.parse("2000-12-31")
                            ),
                            ChildDto(
                                Gender.Male,
                                LocalDate.parse("2001-12-31")
                            ),
                            ChildDto(
                                Gender.Female,
                                LocalDate.parse("2002-12-31")
                            )
                        )
                    )
                )
            }
        }
        assertThat(exception.cause).isInstanceOf(ConstraintViolationException::class.java)
        assertThat(exception.cause?.message).isEqualTo("register.clientDto.phoneNumber: must match \"\\d{11,15}\"")
    }

    @Test
    fun `Should fail - too long phone number`() {
        val exception = assertThrows<NestedServletException> {
            mvc.post("/clients/register") {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(
                    ClientDto(
                        "1234567890123456", 3, "Jon",
                        childrenInfo = listOf(
                            ChildDto(
                                Gender.Male,
                                LocalDate.parse("2000-12-31")
                            ),
                            ChildDto(
                                Gender.Male,
                                LocalDate.parse("2001-12-31")
                            ),
                            ChildDto(
                                Gender.Female,
                                LocalDate.parse("2002-12-31")
                            )
                        )
                    )
                )
            }
        }
        assertThat(exception.cause).isInstanceOf(ConstraintViolationException::class.java)
        assertThat(exception.cause?.message).isEqualTo("register.clientDto.phoneNumber: must match \"\\d{11,15}\"")
    }

    @Test
    internal fun `Should fail - negative children`() {
        val exception = assertThrows<NestedServletException> {
            mvc.post("/clients/register") {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(
                    ClientDto(
                        "12345678901", -1, "Jon",
                        childrenInfo = listOf(
                            ChildDto(
                                Gender.Male,
                                LocalDate.parse("2000-12-31")
                            ),
                            ChildDto(
                                Gender.Male,
                                LocalDate.parse("2001-12-31")
                            ),
                            ChildDto(
                                Gender.Female,
                                LocalDate.parse("2002-12-31")
                            )
                        )
                    )
                )
            }
        }
        assertThat(exception.cause).isInstanceOf(ConstraintViolationException::class.java)
        assertThat(exception.cause?.message).isEqualTo("register.clientDto.children: must be greater than or equal to 0")
    }

    @Test
    @Sql("/scripts/client-registration.sql")
    fun `Should fail - already registered`() {
        val phoneNumber = "12345678901"
        val children = 3
        val name = "John Dow"
        assertThat(runWithRole {
            repository.findByPhoneNumber(phoneNumber)
        }).isNotNull

        mvc.post("/clients/register") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(
                ClientDto(
                    phoneNumber, children, name,
                    childrenInfo = listOf(
                        ChildDto(
                            Gender.Male,
                            LocalDate.parse("2000-12-31")
                        ),
                        ChildDto(
                            Gender.Male,
                            LocalDate.parse("2001-12-31")
                        ),
                        ChildDto(
                            Gender.Female,
                            LocalDate.parse("2002-12-31")
                        )
                    )
                )
            )
        }.andExpect {
            status { isFound }
        }
    }

    @Test
    @Sql("/scripts/client-registration.sql")
    fun `Should fail - inconsistent children quantity`() {
        val phoneNumber = "12345678901"
        val children = 3
        val name = "John Dow"
        assertThat(runWithRole {
            repository.findByPhoneNumber(phoneNumber)
        }).isNotNull

        mvc.post("/clients/register") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(
                ClientDto(
                    phoneNumber, children, name,
                    childrenInfo = listOf(
                        ChildDto(
                            Gender.Male,
                            LocalDate.parse("2000-12-31")
                        ),
                        ChildDto(
                            Gender.Male,
                            LocalDate.parse("2001-12-31")
                        )
                    )
                )
            )
        }.andExpect {
            status { isBadRequest }
        }
    }

    @Test
    fun `New client registration`() {
        val phoneNumber = "12345678901"
        val children = 3
        val name = "John Dow"

        assertThat(runWithRole {
            repository.findByPhoneNumber(phoneNumber)
        }).isNull()
        assertThat(childRepository.count()).isZero()

        val temporalKey = RandomString.make(128)
        val pin = "1234"
        every { randomGenerator.generateString(128) }.answers { temporalKey }
        every { randomGenerator.generateIntString(4) }.answers { pin }

        every { smsProvider.sendPin(phoneNumber, pin) }.answers { }

        mvc.post("/clients/register") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(
                ClientDto(
                    phoneNumber, children, name,
                    childrenInfo = listOf(
                        ChildDto(
                            Gender.Male,
                            LocalDate.parse("2000-12-31")
                        ),
                        ChildDto(
                            Gender.Male,
                            LocalDate.parse("2001-12-31")
                        ),
                        ChildDto(
                            Gender.Female,
                            LocalDate.parse("2002-12-31")
                        )
                    )
                )
            )
        }.andExpect {
            status { isCreated }
        }

        val client = runWithRole { repository.findByPhoneNumber(phoneNumber) }!!
        val childrenInfo = childRepository.findByClientOrderByBirthDate(client)!!
        assertThat(childrenInfo.size).isEqualTo(3)
        assertThat(client).isEqualToIgnoringGivenFields(
            Client(phoneNumber, children, name, temporalKey, pin), "id"
        )

        verify { smsProvider.sendPin(phoneNumber, pin) }
    }

    @Test
    @Sql("/scripts/client-registration.sql")
    fun `Get client info - invalid phone number`() {
        val phoneNumber = "12345678909"
        val key = "123"

        mvc.get("/client/$phoneNumber/$key").andExpect {
            status { isUnauthorized }
        }
    }

    @Test
    @Sql("/scripts/client-registration.sql")
    internal fun `Get client info - invalid key`() {
        val phoneNumber = "12345678901"
        val key = "123x"

        mvc.get("/client/$phoneNumber/$key").andExpect {
            status { isUnauthorized }
        }
    }

    @Test
    @Sql("/scripts/client-registration.sql")
    internal fun `Get client info`() {
        val phoneNumber = "12345678901"
        val key = "123"

        mvc.get("/client/$phoneNumber/$key").andExpect {
            status { isOk }
            jsonPath("\$.name") { value("Jon Dow") }
            jsonPath("\$.id") { value(1) }
            jsonPath("\$.children") { value(1) }
            jsonPath("\$.childrenInfo", Matchers.hasSize<Child>(3))
        }
    }

    @Test
    @Sql("/scripts/client-registration.sql")
    internal fun `Update client data - client not found`() {
        val phoneNumber = "12345678909"
        val key = "123"
        val children = 3
        val name = "John Dow 2"

        mvc.patch("/client") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(UpdateClientDto(phoneNumber, key, children, name, emptyList()))
        }.andExpect {
            status { isUnauthorized }
        }
    }

    @Test
    @Sql("/scripts/client-registration.sql")
    internal fun `Update client data - an invalid key`() {
        val phoneNumber = "12345678901"
        val key = "123x"
        val children = 3
        val name = "John Dow 2"

        mvc.patch("/client") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(UpdateClientDto(phoneNumber, key, children, name, emptyList()))
        }.andExpect {
            status { isUnauthorized }
        }
    }

    @Test
    @Sql("/scripts/client-registration.sql")
    internal fun `Update client data`() {
        val phoneNumber = "12345678901"
        val key = "123"
        val children = 3
        val name = "John Dow 2"

        assertThat(runWithRole {
            repository.findByPhoneNumber(phoneNumber)
        }).isEqualToIgnoringGivenFields(
            Client(phoneNumber, 1, "Jon Dow", key = key), "id"
        )

        mvc.patch("/client") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(
                UpdateClientDto(
                    phoneNumber, key, children, name,
                    listOf(
                        UpdateChildDto(
                            Gender.Male,
                            LocalDate.parse("2001-10-30"),
                            2
                        ),
                        UpdateChildDto(
                            Gender.Female,
                            LocalDate.parse("2002-10-30")
                        ),
                        UpdateChildDto(
                            Gender.Female,
                            LocalDate.parse("2003-10-30")
                        )
                    )
                )
            )
        }.andExpect {
            status { isOk }
        }

        assertThat(runWithRole {
            repository.findByPhoneNumber(phoneNumber)
        }).isEqualToIgnoringGivenFields(
            Client(phoneNumber, children, name, key = key), "id"
        )
    }

    @Test
    fun `Login - not registered`() {
        val phoneNumber = "12345678901"

        assertThat(runWithRole {
            repository.findByPhoneNumber(phoneNumber)
        }).isNull()

        mvc.post("/clients/login") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(ClientDto(phoneNumber, 0, "", emptyList()))
        }.andExpect {
            status { isUnauthorized }
        }
    }

    @Test
    @Sql("/scripts/client-registration.sql")
    fun `Login - ok`() {
        val phoneNumber = "12345678901"

        assertThat(runWithRole {
            repository.findByPhoneNumber(phoneNumber)
        }).isNotNull

        val temporalKey = RandomString.make(128)
        val pin = "1234"
        every { randomGenerator.generateString(128) }.answers { temporalKey }
        every { randomGenerator.generateIntString(4) }.answers { pin }

        every { smsProvider.sendPin(phoneNumber, pin) }.answers { }

        mvc.post("/clients/login") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(ClientDto(phoneNumber, 0, "", emptyList()))
        }.andExpect {
            status { isOk }
        }

        assertThat(runWithRole {
            repository.findByPhoneNumber(phoneNumber)
        }).isEqualToIgnoringGivenFields(
            Client(phoneNumber, 1, "Jon Dow", temporalKey, pin, "123"), "id", "childrenInfo"
        )

        verify { smsProvider.sendPin(phoneNumber, pin) }
    }

    @Test
    @Sql("/scripts/client-registration.sql")
    internal fun `PIN validation - ok`() {
        val phoneNumber = "12345678902"
        val pin = "1221"
        assertThat(runWithRole {
            repository.findByPhoneNumber(phoneNumber)
        }).isNotNull

        mvc.post("/clients/validate") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(ClientValidationDto(phoneNumber, pin))
        }.andExpect {
            status { isOk }
            content { string("\"some-test-key\"") }
        }
    }

    @Test
    internal fun `PIN validation - phone number not found`() {
        val phoneNumber = "12345678909"
        val pin = "1111"
        assertThat(runWithRole {
            repository.findByPhoneNumber(phoneNumber)
        }).isNull()

        mvc.post("/clients/validate") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(ClientValidationDto(phoneNumber, pin))
        }.andExpect {
            status { isUnauthorized }
        }
    }

    @Test
    @Sql("/scripts/client-registration.sql")
    internal fun `PIN validation - invalid PIN`() {
        val phoneNumber = "12345678902"
        val pin = "1111"
        assertThat(runWithRole {
            repository.findByPhoneNumber(phoneNumber)
        }).isNotNull

        mvc.post("/clients/validate") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(ClientValidationDto(phoneNumber, pin))
        }.andExpect {
            status { isUnauthorized }
        }
    }
}
