package kz.oinshyk.back

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.servlet.MockMvc
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Transactional
abstract class BaseMvcIntegrationTest {
    @Autowired
    protected lateinit var mvc: MockMvc

    @Autowired
    protected lateinit var mapper: ObjectMapper
}
