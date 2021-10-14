package kz.oinshyk.back.dictionary.domain.port

import kz.oinshyk.back.dictionary.domain.entity.City
import org.springframework.data.repository.CrudRepository
import org.springframework.security.access.prepost.PreAuthorize
import java.util.*

@PreAuthorize("hasRole('ADMIN')")
interface CityRepository : CrudRepository<City, Long> {

    @PreAuthorize("permitAll()")
    override fun findAll(): MutableIterable<City>

    @PreAuthorize("permitAll()")
    override fun findById(id: Long): Optional<City>
}
