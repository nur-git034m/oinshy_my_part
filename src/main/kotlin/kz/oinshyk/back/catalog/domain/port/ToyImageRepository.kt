package kz.oinshyk.back.catalog.domain.port

import kz.oinshyk.back.catalog.domain.entity.Toy
import kz.oinshyk.back.catalog.domain.entity.ToyImage
import org.springframework.data.repository.CrudRepository
import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize("hasRole('ADMIN')")
interface ToyImageRepository : CrudRepository<ToyImage, Long> {

    @Suppress("unused")
    fun findByToyOrderByFileName(toy: Toy): Iterable<ToyImage>
}
