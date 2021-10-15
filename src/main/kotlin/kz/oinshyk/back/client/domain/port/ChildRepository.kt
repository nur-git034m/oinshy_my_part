package kz.oinshyk.back.client.domain.port

import kz.oinshyk.back.client.domain.entity.Child
import kz.oinshyk.back.client.domain.entity.Client
import org.springframework.data.repository.CrudRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource(exported = false)
interface ChildRepository : CrudRepository<Child, Long> {
    fun findByClientOrderByBirthDate(client: Client): List<Child>?
}
