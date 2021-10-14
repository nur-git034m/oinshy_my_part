package kz.oinshyk.back.client.domain.port

import kz.oinshyk.back.BaseDataIntegrationTest
import kz.oinshyk.back.client.domain.entity.Child
import kz.oinshyk.back.client.domain.entity.Gender
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import java.time.LocalDate

class ChildRepositoryTest(
    private val clientRepository: ClientRepository,
    private val repository: ChildRepository
) : BaseDataIntegrationTest() {

    @Test
    @Sql("/scripts/client-registration.sql")
    fun `Update 3 children`() {
        val client = clientRepository.findByPhoneNumber("12345678901")!!

        assertThat(repository.count()).isEqualTo(3)
        assertThat(repository.findByClientOrderByBirthDate(client)).isEqualTo(
            listOf(
                Child(client, Gender.Male, LocalDate.parse("2000-12-31")).apply { id = 1 },
                Child(client, Gender.Male, LocalDate.parse("2001-12-31")).apply { id = 2 },
                Child(client, Gender.Female, LocalDate.parse("2002-12-31")).apply { id = 3 }
            )
        )

        repository.saveAll(
            listOf(
                Child(client, Gender.Female, LocalDate.parse("2000-12-30")).apply { id = 1 },
                Child(client, Gender.Male, LocalDate.parse("2001-12-30")).apply { id = 2 },
                Child(client, Gender.Male, LocalDate.parse("2002-12-30")).apply { id = 3 },
                Child(client, Gender.Male, LocalDate.parse("2003-12-30")).apply { id = 4 },
                Child(client, Gender.Male, LocalDate.parse("2003-12-30")).apply { id = 5 },
                Child(client, Gender.Male, LocalDate.parse("2004-12-30"))
            )
        )

        assertThat(repository.count()).isEqualTo(6)
        assertThat(repository.findByClientOrderByBirthDate(client)).isEqualTo(
            listOf(
                Child(client, Gender.Female, LocalDate.parse("2000-12-30")).apply { id = 1 },
                Child(client, Gender.Male, LocalDate.parse("2001-12-30")).apply { id = 2 },
                Child(client, Gender.Male, LocalDate.parse("2002-12-30")).apply { id = 3 },
                Child(client, Gender.Male, LocalDate.parse("2003-12-30")).apply { id = 4 },
                Child(client, Gender.Male, LocalDate.parse("2003-12-30")).apply { id = 5 },
                Child(client, Gender.Male, LocalDate.parse("2004-12-30")).apply { id = 6 }
            )
        )
    }
}
