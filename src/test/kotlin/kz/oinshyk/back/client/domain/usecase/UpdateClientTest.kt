package kz.oinshyk.back.client.domain.usecase

import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.verifySequence
import kz.oinshyk.back.client.app.web.UpdateChildDto
import kz.oinshyk.back.client.app.web.UpdateClientDto
import kz.oinshyk.back.client.domain.entity.Child
import kz.oinshyk.back.client.domain.entity.Client
import kz.oinshyk.back.client.domain.entity.Gender
import kz.oinshyk.back.client.domain.port.ChildRepository
import kz.oinshyk.back.client.domain.port.ClientRepository
import kz.oinshyk.back.client.domain.service.FindClient
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDate

@ExtendWith(MockKExtension::class)
internal class UpdateClientTest {

    private lateinit var updateClient: UpdateClient

    @MockK
    private lateinit var findClient: FindClient

    @MockK
    private lateinit var clientRepository: ClientRepository

    @MockK
    private lateinit var childRepository: ChildRepository

    @BeforeEach
    fun setUp() {
        updateClient = UpdateClient(findClient, clientRepository, childRepository)
    }

    @Test
    fun update() {
        val phoneNumber = "12345678901"

        every { findClient.find(phoneNumber, "123") } returns Client(
            phoneNumber,
            2,
            "Mike"
        )

        val client = Client(
            phoneNumber,
            3,
            "Jon"
        )
        every { clientRepository.save(Client(phoneNumber, 3, "Jon")) } returns client

        every { childRepository.findByClientOrderByBirthDate(client) } returns listOf(
            Child(client, Gender.Male, LocalDate.parse("2000-11-30")).apply { id = 1 },
            Child(client, Gender.Male, LocalDate.parse("2001-11-30")).apply { id = 2 }
        )
        every {
            childRepository.deleteAll(
                listOf(Child(client, Gender.Male, LocalDate.parse("2000-11-30")).apply { id = 1 })
            )
        } just Runs
        every {
            childRepository.saveAll(
                listOf(
                    Child(client, Gender.Male, LocalDate.parse("2001-10-30")).apply { id = 2 },
                    Child(client, Gender.Female, LocalDate.parse("2002-10-30")),
                    Child(client, Gender.Female, LocalDate.parse("2003-10-30"))
                )
            )
        } returns emptyList()

        updateClient.update(
            UpdateClientDto(
                phoneNumber,
                "123",
                3,
                "Jon",
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

        verifySequence {
            findClient.find(phoneNumber, "123")

            clientRepository.save(Client(phoneNumber, 3, "Jon"))

            childRepository.findByClientOrderByBirthDate(client)
            childRepository.deleteAll(
                listOf(Child(client, Gender.Male, LocalDate.parse("2000-11-30")).apply { id = 1 })
            )
            childRepository.saveAll(
                listOf(
                    Child(client, Gender.Male, LocalDate.parse("2001-10-30")).apply { id = 2 },
                    Child(client, Gender.Female, LocalDate.parse("2002-10-30")),
                    Child(client, Gender.Female, LocalDate.parse("2003-10-30"))
                )
            )
        }
    }
}
