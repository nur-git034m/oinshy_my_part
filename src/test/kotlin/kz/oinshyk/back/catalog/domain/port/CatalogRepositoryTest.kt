package kz.oinshyk.back.catalog.domain.port

import kz.oinshyk.back.BaseDataIntegrationTest
import kz.oinshyk.back.catalog.domain.entity.Category
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager

@DataJpaTest
class CatalogRepositoryTest(
        val entityManager: TestEntityManager,
        val categoryRepository: CategoryRepository
) : BaseDataIntegrationTest() {

    @Test
    fun `Save and find a category`() {
        val cat1 = Category("Cat1", "img1")
        entityManager.persist(cat1)
        entityManager.flush()
        val found = categoryRepository.findAll()
        assertThat(found).hasSize(1)
        assertThat(found.first()).isEqualTo(cat1)
    }
}
