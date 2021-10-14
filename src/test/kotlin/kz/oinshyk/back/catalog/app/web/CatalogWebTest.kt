package kz.oinshyk.back.catalog.app.web

import kz.oinshyk.back.BaseMvcIntegrationTest
import kz.oinshyk.back.catalog.domain.entity.Category
import kz.oinshyk.back.catalog.domain.entity.Toy
import kz.oinshyk.back.catalog.domain.usecase.CategoryDto
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.get

@Sql("/scripts/catalog.sql")
internal class CatalogWebTest : BaseMvcIntegrationTest() {

    @Test
    fun `List categories`() {
        mvc.get("/catalog/categories").andExpect {
            status { isOk }
            jsonPath("\$", hasSize<Category>(3))
            jsonPath("\$[0].id") { value(1) }
        }
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `should return a tree of all categories`() {
        mvc.get("/catalog/tree-of-categories").andExpect {
            status { isOk }
            jsonPath("\$", hasSize<CategoryDto>(3))
            jsonPath("\$[0].id") { value(1) }
            jsonPath("\$[0].name") { value("Cat 1") }
            jsonPath("\$[0].url") { value("http://localhost/v1/categories/1") }
            jsonPath("\$[0].children", hasSize<CategoryDto>(1))
            jsonPath("\$[0].children[0].id") { value(4) }
            jsonPath("\$[0].children[0].name") { value("Cat 4") }
            jsonPath("\$[1].id") { value(2) }
            jsonPath("\$[2].id") { value(3) }
        }
    }

    @Test
    fun `List subcategories`() {
        mvc.get("/catalog/categories/1").andExpect {
            status { isOk }
            jsonPath("\$", hasSize<Category>(1))
            jsonPath("\$[0].name") { value("Cat 4") }
        }
    }

    @Test
    fun `List subcategories - no subs`() {
        mvc.get("/catalog/categories/2").andExpect {
            status { isOk }
            jsonPath("\$", hasSize<Category>(0))
        }
    }

    @Test
    fun `List toys for a category`() {
        mvc.get("/catalog/category/4").andExpect {
            status { isOk }
            jsonPath("\$", hasSize<Toy>(1))
            jsonPath("\$[0].id") { value(1) }
        }
    }

    @Test
    fun `Get toys on main page`() {
        mvc.get("/catalog/main-page-toys").andExpect {
            status { isOk }
            jsonPath("\$", hasSize<Toy>(1))
            jsonPath("\$[0].id") { value(1) }
        }
    }

    @Test
    fun `Search toys`() {
        mvc.get("/catalog/search/toY").andExpect {
            status { isOk }
            jsonPath("\$", hasSize<Toy>(1))
            jsonPath("\$[0].id") { value(1) }
        }
    }

    @Test
    fun `Search by SKU`() {
        mvc.get("/v1/toys/search/findBySkuIgnoreCaseContainingOrderByName?sku=123").andExpect {
            status { isOk }
            jsonPath("\$._embedded.toys", hasSize<Toy>(1))
            jsonPath("\$._embedded.toys[0].name") {
                value("Toy 1")
            }
        }
    }
}
