package kz.oinshyk.back.catalog.domain.usecase

import kz.oinshyk.back.catalog.domain.entity.Category
import kz.oinshyk.back.catalog.domain.port.CategoryRepository
import org.springframework.data.rest.webmvc.support.RepositoryEntityLinks
import org.springframework.stereotype.Service

@Service
class GetTreeOfCategories(
    private val categoryRepository: CategoryRepository,
    private val entityLinks: RepositoryEntityLinks
) {
    operator fun invoke(): List<CategoryDto> {
        val roots = mutableListOf<CategoryDto>()
        val parentsById = mutableMapOf<Long, CategoryDto>()
        val childrenByParentId = mutableMapOf<Long, MutableSet<CategoryDto>>()

        categoryRepository.findAll().forEach { category ->
            val dto = CategoryDto.from(category, entityLinks)
            parentsById[dto.id] = dto
            childrenByParentId[dto.id]?.run { dto.children.addAll(this.toList().sortedBy { it.name }) }

            if (!category.hasParent) {
                roots.add(dto)
            } else {
                val parentId = category.parent!!.id!!

                childrenByParentId.putIfAbsent(parentId, mutableSetOf(dto))?.add(dto)
                parentsById[parentId]?.children?.run {
                    add(dto)
                    sortBy { it.name }
                }
            }
        }

        roots.sortBy { it.name }

        return roots
    }
}

data class CategoryDto(
    val id: Long,
    val name: String,
    val url: String,
    val children: MutableList<CategoryDto> = mutableListOf()
) {
    companion object {
        fun from(category: Category, entityLinks: RepositoryEntityLinks) = CategoryDto(
            category.id!!,
            category.name,
            entityLinks.linkToItemResource(category) { it.id }.href
        )
    }
}
