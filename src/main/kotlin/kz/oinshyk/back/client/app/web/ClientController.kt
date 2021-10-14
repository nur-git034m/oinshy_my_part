package kz.oinshyk.back.client.app.web

import kz.oinshyk.back.client.domain.entity.Gender
import kz.oinshyk.back.client.domain.usecase.GetClientInfo
import kz.oinshyk.back.client.domain.usecase.UpdateClient
import kz.oinshyk.back.common.app.ApiController
import kz.oinshyk.back.common.domain.entity.BaseClientDto
import org.springframework.data.rest.webmvc.BasePathAwareController
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import java.time.LocalDate
import javax.validation.Valid
import javax.validation.constraints.PastOrPresent
import javax.validation.constraints.PositiveOrZero

@ApiController("client")
@BasePathAwareController
class ClientController(
    private val updateClient: UpdateClient,
    private val getClientInfo: GetClientInfo
) {

    // Flutter's http package doesn't support body in GET requests
    @GetMapping("{phoneNumber}/{key}")
    fun get(@PathVariable phoneNumber: String, @PathVariable key: String) = getClientInfo.get(phoneNumber, key)

    @PatchMapping
    fun update(@Valid @RequestBody dto: UpdateClientDto) = updateClient.update(dto)

    @GetMapping("admin-info/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun getAdminInfo(@PathVariable id: Long) = getClientInfo.get(id)
}

class UpdateClientDto(
    phoneNumber: String,
    key: String,
    @get:PositiveOrZero
    val children: Int,
    val name: String,
    val childrenInfo: List<UpdateChildDto>
) : BaseClientDto(phoneNumber, key)

data class UpdateChildDto(
    val gender: Gender,

    @get:PastOrPresent
    val birthDate: LocalDate,

    val id: Long? = null
)
