package kz.oinshyk.back.exchange.app

import kz.oinshyk.back.common.app.ApiController
import kz.oinshyk.back.exchange.domain.usecase.CreateToy
import kz.oinshyk.back.exchange.domain.usecase.ToyDto
import kz.oinshyk.back.exchange.domain.usecase.UpdateToy
import org.springframework.data.rest.webmvc.BasePathAwareController
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import javax.validation.Valid

@ApiController("exchange")
@BasePathAwareController
@PreAuthorize("hasRole('EXCHANGER')")
class ExchangeController(
    private val createToy: CreateToy,
    private val updateToy: UpdateToy
) {

    @PostMapping
    fun create(@Valid @RequestBody dto: ToyDto) = createToy(dto)

    @PatchMapping
    fun update(@Valid @RequestBody dto: ToyDto) = updateToy(dto)
}
