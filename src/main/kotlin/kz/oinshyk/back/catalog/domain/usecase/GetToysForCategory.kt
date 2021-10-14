package kz.oinshyk.back.catalog.domain.usecase

import kz.oinshyk.back.catalog.domain.entity.Toy
import kz.oinshyk.back.catalog.domain.port.CategoryRepository
import kz.oinshyk.back.catalog.domain.port.ToyRepository
import kz.oinshyk.back.catalog.infra.CategoryNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class GetToysForCategory(
        private val categoryRepository: CategoryRepository,
        private val toyRepository: ToyRepository
) {
    fun find(id: Long): Iterable<Toy> {
        val category = categoryRepository.findByIdOrNull(id) ?: throw CategoryNotFoundException()
        return toyRepository.findByCategoryAndShowIsTrue(category)
    }
}
