package kz.oinshyk.back.client.domain.usecase

import kz.oinshyk.back.AppConfig
import kz.oinshyk.back.client.app.web.dto.ClientDto
import kz.oinshyk.back.client.app.web.dto.ClientValidationDto
import kz.oinshyk.back.client.domain.entity.Child
import kz.oinshyk.back.client.domain.entity.Client
import kz.oinshyk.back.client.domain.port.ChildRepository
import kz.oinshyk.back.client.domain.port.ClientRepository
import kz.oinshyk.back.security.domain.port.RandomGenerator
import kz.oinshyk.back.security.infra.runWithRole
import kz.oinshyk.back.sms.domain.port.SmsProvider
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.ResponseStatus

private const val keyLength = 128
private const val pinLength = 4

@Service
class RegisterClient(
    private val repository: ClientRepository,
    private val childRepository: ChildRepository,
    private val smsProvider: SmsProvider,
    private val randomGenerator: RandomGenerator,
    private val appConfig: AppConfig
) {

    fun register(dto: ClientDto) {
        if (dto.children != dto.childrenInfo.size) throw InconsistentChildrenInfoException()

        runWithRole {
            if (repository.findByPhoneNumber(dto.phoneNumber) != null)
                throw ClientAlreadyRegisteredException()

            val temporalKey = randomGenerator.generateString(keyLength)
            val pin = randomGenerator.generateIntString(pinLength)

            val client = repository.save(Client(dto.phoneNumber, dto.children, dto.name, temporalKey, pin))

            dto.childrenInfo.forEach {
                val child = Child(client, it.gender, it.birthDate)
                childRepository.save(child)
            }

            smsProvider.sendPin(dto.phoneNumber, pin)
        }

    }

    fun login(dto: ClientDto) {
        if (dto.phoneNumber == appConfig.testPhoneNumber) return

        runWithRole {
            val client = repository.findByPhoneNumber(dto.phoneNumber) ?: throw ClientNotFoundException()

            val temporalKey = randomGenerator.generateString(keyLength)
            val pin = randomGenerator.generateIntString(pinLength)

            client.temporalKey = temporalKey
            client.pin = pin
            repository.save(client)

            smsProvider.sendPin(dto.phoneNumber, pin)
        }
    }

    fun validate(dto: ClientValidationDto): String {
        return runWithRole {
            val client = repository.findByPhoneNumber(dto.phoneNumber)
                ?: throw ClientNotFoundException()

            if (dto.phoneNumber == appConfig.testPhoneNumber) {
                if (dto.pin != appConfig.testPin) throw ClientValidationException()
            } else {
                if (client.pin != dto.pin) throw ClientValidationException()

                client.key = client.temporalKey
                client.temporalKey = null
                client.pin = null
                repository.save(client)
            }

            client.key
        }!!
    }
}

@ResponseStatus(HttpStatus.BAD_REQUEST)
class InconsistentChildrenInfoException : RuntimeException()

@ResponseStatus(HttpStatus.FOUND)
class ClientAlreadyRegisteredException : RuntimeException()

@ResponseStatus(HttpStatus.UNAUTHORIZED)
class ClientValidationException : RuntimeException()

@ResponseStatus(HttpStatus.UNAUTHORIZED)
class ClientNotFoundException : RuntimeException()
