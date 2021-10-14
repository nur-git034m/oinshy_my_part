package kz.oinshyk.back.catalog.domain.usecase

import kz.oinshyk.back.catalog.domain.port.ToyRepository
import org.springframework.stereotype.Service

@Service
class SearchToys(
        private val toyRepository: ToyRepository
) {
    fun search(text: String) = toyRepository.findByNameIgnoreCaseContainingAndShowIsTrueOrderByName(text)
}
