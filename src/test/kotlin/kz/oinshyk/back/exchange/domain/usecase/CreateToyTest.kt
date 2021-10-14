package kz.oinshyk.back.exchange.domain.usecase

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kz.oinshyk.back.catalog.domain.entity.Category
import kz.oinshyk.back.catalog.domain.entity.Toy
import kz.oinshyk.back.catalog.domain.port.ToyRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal

@ExtendWith(MockKExtension::class)
internal class CreateToyTest {
    private lateinit var createToy: CreateToy

    private lateinit var response: ExchangeResponse

    @MockK
    private lateinit var toyRepository: ToyRepository

    @BeforeEach
    fun setUp() {
        createToy = CreateToy(toyRepository)
    }

    private fun invoke() {
        response = createToy(
            ToyDto(
                "sku",
                "name",
                "description",
                BigDecimal(1234),
                BigDecimal(1000),
                10
            )
        )
    }

    @Test
    fun `Duplicate sku`() {
        every { toyRepository.findBySku("sku") } returns Toy(
            "sku",
            "name",
            BigDecimal(1),
            BigDecimal(1),
            10,
            Category("cat 1", "img")
        )

        invoke()

        assertThat(response.result).isEqualTo(ExchangeResult.DuplicateSku)
    }

    @Test
    fun `Insert a new toy`() {
        every { toyRepository.findBySku("sku") } returns null

        val toy = Toy(
            "sku",
            "name",
            BigDecimal(1234),
            BigDecimal(1000),
            10,
            null,
            "description"
        )
        every { toyRepository.save(toy) } returns toy

        invoke()

        assertThat(response.result).isEqualTo(ExchangeResult.Ok)

        verify { toyRepository.save(toy) }
    }
}
