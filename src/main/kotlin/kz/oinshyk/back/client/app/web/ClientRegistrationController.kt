package kz.oinshyk.back.client.app.web

import kz.oinshyk.back.client.app.web.dto.ClientDto
import kz.oinshyk.back.client.app.web.dto.ClientValidationDto
import kz.oinshyk.back.client.domain.usecase.RegisterClient
import org.springframework.data.rest.webmvc.BasePathAwareController
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@Validated
@BasePathAwareController
@RequestMapping("clients")
class ClientRegistrationController(
        private val registerClient: RegisterClient
) {

    @PostMapping("register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@Valid @RequestBody clientDto: ClientDto) = registerClient.register(clientDto)

    @PostMapping("login")
    fun login(@Valid @RequestBody clientDto: ClientDto) = registerClient.login(clientDto)

    @PostMapping("validate")
    fun validate(@Valid @RequestBody clientValidationDto: ClientValidationDto) = registerClient.validate(clientValidationDto)
}
