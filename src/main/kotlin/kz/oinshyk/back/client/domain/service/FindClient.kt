package kz.oinshyk.back.client.domain.service

import kz.oinshyk.back.client.domain.port.ClientRepository
import kz.oinshyk.back.client.domain.usecase.ClientNotFoundException
import kz.oinshyk.back.security.infra.runWithRole
import org.springframework.stereotype.Service

@Service
class FindClient(
        private val repository: ClientRepository
) {

    fun find(phoneNumber: String, key: String) = runWithRole {
        repository.findByPhoneNumberAndKey(phoneNumber, key) ?: throw ClientNotFoundException()
    }

}
