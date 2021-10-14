package kz.oinshyk.back.catalog.infra

import kz.oinshyk.back.catalog.domain.entity.Toy
import org.springframework.data.rest.core.annotation.HandleAfterDelete
import org.springframework.data.rest.core.annotation.RepositoryEventHandler
import org.springframework.stereotype.Component

@Component
@RepositoryEventHandler(Toy::class)
class ToyHandler(
        private val fileManager: FileManager
) {

    @HandleAfterDelete
    fun afterDeleted(toy: Toy) {
        fileManager.deleteToyImages(toy)
    }
}
