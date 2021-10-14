package kz.oinshyk.back.security.infra

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class RandomGeneratorImplTest {

    @Test
    fun generateString() {
        val generator = RandomGeneratorImpl()
        println(generator.generateString(128))
        Assertions.assertThat(generator.generateString(128).length).isEqualTo(128)
    }

    @Test
    fun generateIntString() {
        val generator = RandomGeneratorImpl()
        println(generator.generateIntString(4))
        Assertions.assertThat(generator.generateIntString(4).length).isEqualTo(4)
    }
}
