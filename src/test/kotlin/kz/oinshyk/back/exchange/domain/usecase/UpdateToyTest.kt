package kz.oinshyk.back.exchange.domain.usecase

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kz.oinshyk.back.catalog.domain.entity.Category
import kz.oinshyk.back.catalog.domain.entity.Toy
import kz.oinshyk.back.catalog.domain.port.ToyRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal

@ExtendWith(MockKExtension::class)
internal class UpdateToyTest {
    private lateinit var updateToy: UpdateToy

    private lateinit var response: ExchangeResponse

    @MockK
    private lateinit var toyRepository: ToyRepository

    @BeforeEach
    internal fun setUp() {
        updateToy = UpdateToy(toyRepository)
    }

    private fun invoke() {
        response = updateToy(
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
    fun `Toy not found`() {
        every { toyRepository.findBySku("sku") } returns null

        invoke()

        Assertions.assertThat(response.result).isEqualTo(ExchangeResult.ToyNotFound)
    }

    @Test
    fun `Update a toy`() {
        every { toyRepository.findBySku("sku") } returns Toy(
            "sku",
            "old name",
            BigDecimal(12340),
            BigDecimal(10000),
            100,
            Category("old cat 1", "old img"),
            "old description"
        )

        val toy = Toy(
            "sku",
            "name",
            BigDecimal(1234),
            BigDecimal(1000),
            10,
            Category("old cat 1", "old img"),
            "description"
        )
        every { toyRepository.save(toy) } returns toy

        invoke()

        Assertions.assertThat(response.result).isEqualTo(ExchangeResult.Ok)

        verify { toyRepository.save(toy) }
    }
}
