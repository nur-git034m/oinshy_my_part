package kz.oinshyk.back.exchange.domain.usecase

import kz.oinshyk.back.catalog.domain.port.ToyRepository
import kz.oinshyk.back.security.infra.runWithRole
import org.springframework.stereotype.Service

@Service
class UpdateToy(
    val toyRepository: ToyRepository
) {
    operator fun invoke(dto: ToyDto): ExchangeResponse {
        val toy = runWithRole("ADMIN") { toyRepository.findBySku(dto.sku) }
            ?: return ExchangeResponse(ExchangeResult.ToyNotFound)

        with(toy) {
            name = dto.name
            description = dto.description
            price = dto.price
            subscriptionPrice = dto.subscriptionPrice
            quantity = dto.quantity
        }
        runWithRole("ADMIN") { toyRepository.save(toy) }

        return ExchangeResponse(ExchangeResult.Ok)
    }
}
