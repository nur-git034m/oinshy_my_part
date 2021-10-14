package kz.oinshyk.back.catalog.app

import kz.oinshyk.back.catalog.domain.entity.Category
import kz.oinshyk.back.catalog.domain.port.CategoryRepository
import kz.oinshyk.back.catalog.domain.usecase.GetMainPageToys
import kz.oinshyk.back.catalog.domain.usecase.GetToysForCategory
import kz.oinshyk.back.catalog.domain.usecase.GetTreeOfCategories
import kz.oinshyk.back.catalog.domain.usecase.SearchToys
import kz.oinshyk.back.common.app.ApiController
import org.springframework.data.rest.webmvc.BasePathAwareController
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@ApiController("catalog")
@BasePathAwareController
class CatalogController(
    private val getMainPageToys: GetMainPageToys,
    private val searchToys: SearchToys,
    private val getToysForCategory: GetToysForCategory,
    private val categoryRepository: CategoryRepository,
    private val getTreeOfCategories: GetTreeOfCategories
) {
    @GetMapping("categories")
    fun categories() = categoryRepository.findByParentIsNull()

    @GetMapping("categories/{id}")
    fun subCategories(@PathVariable id: Long): Iterable<Category> {
        val category = categoryRepository.findById(id).orElseThrow()
        return categoryRepository.findByParent(category)
    }

    @GetMapping("main-page-toys")
    fun getMainPageToys() = getMainPageToys.get()

    @GetMapping("search/{text}")
    fun search(@PathVariable text: String) = searchToys.search(text)

    @GetMapping("category/{id}")
    fun category(@PathVariable id: Long) = getToysForCategory.find(id)

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("tree-of-categories")
    fun treeOfCategories() = getTreeOfCategories()
}
