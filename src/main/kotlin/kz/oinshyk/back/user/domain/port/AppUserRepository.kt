package kz.oinshyk.back.user.domain.port

import kz.oinshyk.back.user.domain.entity.AppUser
import org.springframework.data.repository.CrudRepository
import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize("hasRole('ADMIN')")
interface AppUserRepository : CrudRepository<AppUser, Long> {

    @PreAuthorize("hasRole('SYSTEM')")
    fun findByLogin(login: String): AppUser?
}
