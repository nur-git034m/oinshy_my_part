package kz.oinshyk.back.catalog.domain.port

import kz.oinshyk.back.catalog.domain.entity.Category
import kz.oinshyk.back.catalog.domain.entity.Toy
import org.springframework.data.repository.CrudRepository
import org.springframework.security.access.prepost.PreAuthorize
import java.util.*

@PreAuthorize("hasRole('ADMIN')")
interface ToyRepository : CrudRepository<Toy, Long> {

    @PreAuthorize("permitAll()")
    override fun findById(id: Long): Optional<Toy>

    @PreAuthorize("permitAll()")
    fun findByCategoryAndShowIsTrue(category: Category): Iterable<Toy>

    @PreAuthorize("permitAll()")
    fun findByNameIgnoreCaseContainingAndShowIsTrueOrderByName(name: String): Iterable<Toy>

    @PreAuthorize("permitAll()")
    fun findBySkuIgnoreCaseContainingOrderByName(sku: String): Iterable<Toy>

    @PreAuthorize("permitAll()")
    fun findByShowIsTrueAndShowOnMainPageIsTrue(): Iterable<Toy>

    fun findBySku(sku: String): Toy?
}
