package kz.oinshyk.back.client.domain.port

import kz.oinshyk.back.BaseDataIntegrationTest
import kz.oinshyk.back.client.domain.entity.ClientOrderStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql

class ClientOrderRepositoryTest(
    private val repository: ClientOrderRepository
) : BaseDataIntegrationTest() {

    @Test
    @Sql("/scripts/orders.sql")
    fun `Find orders by statuses`() {
        assertThat(repository.findByStatusInOrderByOrderedAtDesc(setOf(ClientOrderStatus.Ordered))).hasSize(3)
    }

    @Test
    @Sql("/scripts/orders.sql")
    fun `Find orders by statuses - combination`() {
        assertThat(
            repository.findByStatusInOrderByOrderedAtDesc(
                setOf(
                    ClientOrderStatus.Ordered,
                    ClientOrderStatus.Delivered
                )
            )
        ).hasSize(4)
    }
}
