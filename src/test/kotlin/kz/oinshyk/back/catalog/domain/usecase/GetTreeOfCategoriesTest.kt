package kz.oinshyk.back.catalog.domain.usecase

import io.mockk.every
import io.mockk.mockk
import kz.oinshyk.back.catalog.domain.entity.Category
import kz.oinshyk.back.catalog.domain.port.CategoryRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.data.rest.webmvc.support.RepositoryEntityLinks
import org.springframework.hateoas.Link
import java.util.stream.Stream

internal class GetTreeOfCategoriesTest {

    private val repo = mockk<CategoryRepository>()

    private val entityLinks = mockk<RepositoryEntityLinks>()

    @ParameterizedTest
    @MethodSource("source")
    internal fun `should return`(categories: List<Category>, tree: List<CategoryDto>) {
        every { repo.findAll() } returns categories
        every { entityLinks.linkToItemResource(any<Category>(), any()) } returns Link.of("url")

        assertThat(GetTreeOfCategories(repo, entityLinks)()).isEqualTo(tree)
    }

    companion object {
        @Suppress("unused")
        @JvmStatic
        fun source(): Stream<Arguments>? {
            val p1 = Category("p1", "").apply { id = 1 }

            val p2 = Category("p2", "").apply { id = 2 }
            val c1p2 = Category("c1p2", "", p2).apply { id = 21 }

            val p3 = Category("p3", "").apply { id = 3 }
            val c1p3 = Category("c1p3", "", p3).apply { id = 31 }
            val c1c1p3 = Category("c1c1p3", "", c1p3).apply { id = 311 }

            val p4 = Category("p4", "").apply { id = 4 }
            val c1p4 = Category("c1p4", "", p4).apply { id = 41 }
            val c2p4 = Category("c2p4", "", p4).apply { id = 42 }
            val c1c1p4 = Category("c1c1p4", "", c1p4).apply { id = 411 }
            val c2c1p4 = Category("c2c1p4", "", c1p4).apply { id = 412 }

            val p1dto = CategoryDto(1, "p1", "url")

            val c1p2dto = CategoryDto(21, "c1p2", "url")
            val p2dto = CategoryDto(2, "p2", "url").apply { children.add(c1p2dto) }

            val c1c1p3dto = CategoryDto(311, "c1c1p3", "url")
            val c1p3dto = CategoryDto(31, "c1p3", "url").apply { children.add(c1c1p3dto) }
            val p3dto = CategoryDto(3, "p3", "url").apply { children.add(c1p3dto) }

            val c1c1p4dto = CategoryDto(411, "c1c1p4", "url")
            val c2c1p4dto = CategoryDto(412, "c2c1p4", "url")
            val c1p4dto = CategoryDto(41, "c1p4", "url").apply { children.addAll(listOf(c1c1p4dto, c2c1p4dto)) }
            val c2p4dto = CategoryDto(42, "c2p4", "url")
            val p4dto = CategoryDto(4, "p4", "url").apply { children.addAll(listOf(c1p4dto, c2p4dto)) }

            return Stream.of(
                Arguments.of(listOf(p1), listOf(p1dto)),

                Arguments.of(listOf(p2, c1p2), listOf(p2dto)),
                Arguments.of(listOf(c1p2, p2), listOf(p2dto)),

                Arguments.of(listOf(p3, c1p3, c1c1p3), listOf(p3dto)),
                Arguments.of(listOf(c1p3, p3, c1c1p3), listOf(p3dto)),
                Arguments.of(listOf(c1p3, c1c1p3, p3), listOf(p3dto)),
                Arguments.of(listOf(c1c1p3, c1p3, p3), listOf(p3dto)),

                Arguments.of(listOf(p1, c1c1p3, c1p3, p3), listOf(p1dto, p3dto)),
                Arguments.of(listOf(c1c1p3, c1p3, p3, p1), listOf(p1dto, p3dto)),

                Arguments.of(
                    listOf(c1c1p4, c2c1p4, c2p4, c1p4, p4),
                    listOf(p4dto)
                ),
                Arguments.of(
                    listOf(c1c1p4, c2c1p4, c1p4, c2p4, p4),
                    listOf(p4dto)
                ),
                Arguments.of(
                    listOf(c1p4, c1c1p4, c2c1p4, c2p4, p4),
                    listOf(p4dto)
                ),
                Arguments.of(
                    listOf(c1p4, c2c1p4, c1c1p4, c2p4, p4),
                    listOf(p4dto)
                ),
                Arguments.of(
                    listOf(c2c1p4, c1c1p4, c2p4, c1p4, p4),
                    listOf(p4dto)
                )
            )
        }
    }
}
