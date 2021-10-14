package kz.oinshyk.back.exchange.domain.usecase

import kz.oinshyk.back.catalog.domain.entity.Toy
import kz.oinshyk.back.catalog.domain.port.ToyRepository
import kz.oinshyk.back.security.infra.runWithRole
import org.springframework.stereotype.Service
import java.math.BigDecimal
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Positive
import javax.validation.constraints.PositiveOrZero

@Service
class CreateToy(
    val toyRepository: ToyRepository
) {
    operator fun invoke(dto: ToyDto): ExchangeResponse {
        if (runWithRole("ADMIN") { toyRepository.findBySku(dto.sku) } != null) {
            return ExchangeResponse(ExchangeResult.DuplicateSku)
        }

        runWithRole("ADMIN") {
            toyRepository.save(
                Toy(
                    dto.sku,
                    dto.name,
                    dto.price,
                    dto.subscriptionPrice,
                    dto.quantity,
                    null,
                    dto.description
                )
            )
        }

        return ExchangeResponse(ExchangeResult.Ok)
    }
}

data class ToyDto(
    @get:NotBlank
    val sku: String,

    @get:NotBlank
    val name: String,

    val description: String?,

    @get:Positive
    val price: BigDecimal,

    @get:Positive
    val subscriptionPrice: BigDecimal,

    @get:PositiveOrZero
    val quantity: Int
)

data class ExchangeResponse(val result: ExchangeResult)

enum class ExchangeResult { Ok, DuplicateSku, ToyNotFound }
