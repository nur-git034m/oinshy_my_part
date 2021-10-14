package kz.oinshyk.back.catalog.domain.usecase

import kz.oinshyk.back.catalog.domain.port.ToyRepository
import org.springframework.stereotype.Service

@Service
class GetMainPageToys(
        private val toyRepository: ToyRepository
) {
    fun get() = toyRepository.findByShowIsTrueAndShowOnMainPageIsTrue()
}
