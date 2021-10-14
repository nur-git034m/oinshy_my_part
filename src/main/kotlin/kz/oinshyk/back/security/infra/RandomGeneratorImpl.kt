package kz.oinshyk.back.security.infra

import kz.oinshyk.back.security.domain.port.RandomGenerator
import net.bytebuddy.utility.RandomString
import org.springframework.stereotype.Service
import kotlin.random.Random

@Service
class RandomGeneratorImpl : RandomGenerator {
    override fun generateString(length: Int): String {
        return RandomString.make(length)
    }

    override fun generateIntString(length: Int): String {
        val x = Random.nextInt(10000)
        return String.format("%0${length}d", x).substring(0, length)
    }
}
