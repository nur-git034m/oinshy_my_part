package kz.oinshyk.back.client.app.web.dto

import javax.validation.constraints.Pattern

data class ClientValidationDto(
        @get:Pattern(regexp = """\d{11,15}""")
        val phoneNumber: String,

        @get:Pattern(regexp = """\d{4}""")
        val pin: String
)
