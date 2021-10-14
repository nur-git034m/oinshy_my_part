package kz.oinshyk.back.security.domain.port

interface RandomGenerator {

    fun generateString(length: Int): String

    fun generateIntString(length: Int): String
}
