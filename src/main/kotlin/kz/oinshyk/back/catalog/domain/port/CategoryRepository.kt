package kz.oinshyk.back.catalog.domain.port

import kz.oinshyk.back.catalog.domain.entity.Category
import org.springframework.data.repository.CrudRepository
import org.springframework.security.access.prepost.PreAuthorize

interface CategoryRepository : CrudRepository<Category, Long> {

    @PreAuthorize("hasRole('ADMIN')")
    override fun <S : Category?> save(entity: S): S

    @PreAuthorize("hasRole('ADMIN')")
    override fun <S : Category?> saveAll(entities: MutableIterable<S>): MutableIterable<S>

    @PreAuthorize("hasRole('ADMIN')")
    override fun deleteById(id: Long)

    @PreAuthorize("hasRole('ADMIN')")
    override fun delete(entity: Category)

    @PreAuthorize("hasRole('ADMIN')")
    override fun deleteAll(entities: MutableIterable<Category>)

    @PreAuthorize("hasRole('ADMIN')")
    override fun deleteAll()

    fun findByParentIsNull(): Iterable<Category>

    fun findByParent(category: Category): Iterable<Category>
}
