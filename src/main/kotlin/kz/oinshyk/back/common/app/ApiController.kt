package kz.oinshyk.back.common.app

import org.springframework.core.annotation.AliasFor
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RequestMapping
@RestController
annotation class ApiController(

        @get:AliasFor(annotation = RequestMapping::class)
        val value: String = ""
)
