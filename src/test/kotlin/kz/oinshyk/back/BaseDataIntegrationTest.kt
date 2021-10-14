package kz.oinshyk.back

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.context.TestConstructor
import org.springframework.transaction.annotation.Transactional

@DataJpaTest
@Import(BaseDataIntegrationTest.Config::class)
@Transactional
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
abstract class BaseDataIntegrationTest {

    @TestConfiguration
    internal class Config {
        @Bean
        fun restTemplateBuilder() = RestTemplateBuilder()
    }
}
