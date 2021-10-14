package kz.oinshyk.back.dictionary.domain.port

import kz.oinshyk.back.dictionary.domain.entity.Setting
import org.springframework.data.repository.CrudRepository
import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize("hasRole('ADMIN')")
interface SettingRepository : CrudRepository<Setting, Long> {

    fun findByKey(key: String): Setting?
}
