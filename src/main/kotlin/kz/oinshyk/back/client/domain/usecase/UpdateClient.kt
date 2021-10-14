package kz.oinshyk.back.client.domain.usecase

import kz.oinshyk.back.client.app.web.UpdateClientDto
import kz.oinshyk.back.client.domain.entity.Child
import kz.oinshyk.back.client.domain.port.ChildRepository
import kz.oinshyk.back.client.domain.port.ClientRepository
import kz.oinshyk.back.client.domain.service.FindClient
import kz.oinshyk.back.security.infra.runWithRole
import org.springframework.stereotype.Service

@Service
class UpdateClient(
    private val findClient: FindClient,
    private val clientRepository: ClientRepository,
    private val childRepository: ChildRepository
) {
    fun update(dto: UpdateClientDto) {
        val client = findClient.find(dto.phoneNumber, dto.key)

        client.children = dto.children
        client.name = dto.name
        runWithRole {
            clientRepository.save(client)
        }

        val childrenList = childRepository.findByClientOrderByBirthDate(client) ?: emptyList()
        val childrenIdsToUpdate = dto.childrenInfo.map { it.id }
        childRepository.deleteAll(childrenList.filterNot { childrenIdsToUpdate.contains(it.id) })
        childRepository.saveAll(dto.childrenInfo.map { childDto ->
            val child = childrenList.firstOrNull { it.id == childDto.id }
            child?.apply {
                gender = childDto.gender
                birthDate = childDto.birthDate
            }
                ?: Child(client, childDto.gender, childDto.birthDate)
        })
    }

}
