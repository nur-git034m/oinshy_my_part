package kz.oinshyk.back.catalog.infra

import kz.oinshyk.back.catalog.domain.entity.Category
import org.springframework.data.rest.core.annotation.HandleAfterDelete
import org.springframework.data.rest.core.annotation.RepositoryEventHandler
import org.springframework.stereotype.Component

@Component
@RepositoryEventHandler(Category::class)
class CategoryHandler(
        private val fileManager: FileManager
) {

    @HandleAfterDelete
    fun afterDeleted(category: Category) {
        fileManager.deleteCategoryImage(category)
    }
}
