package kz.oinshyk.back.cart.app.web

import kz.oinshyk.back.cart.app.web.dto.AddToyToCartDto
import kz.oinshyk.back.cart.app.web.dto.UpdateCartItemQuantityDto
import kz.oinshyk.back.cart.domain.usecase.*
import kz.oinshyk.back.common.domain.entity.BaseClientDto
import org.springframework.data.rest.webmvc.BasePathAwareController
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@Validated
@BasePathAwareController
@RequestMapping("cart")
class CartController(
        private val addToyToCart: AddToyToCart,
        private val getCartContents: GetCartContents,
        private val updateCartItemQuantity: UpdateCartItemQuantity,
        private val deleteItemFromCart: DeleteItemFromCart,
        private val deleteCart: DeleteCart
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun addToy(@Valid @RequestBody addToyToCartDto: AddToyToCartDto) = addToyToCart.add(addToyToCartDto)

    @GetMapping("{phoneNumber}/{key}")
    fun get(@PathVariable phoneNumber: String, @PathVariable key: String) = getCartContents.contents(phoneNumber, key)

    @PutMapping("{itemId}")
    @ResponseStatus(HttpStatus.FOUND)
    fun put(@Valid @RequestBody dto: UpdateCartItemQuantityDto, @PathVariable itemId: Long) =
            updateCartItemQuantity.update(dto, itemId)

    // flutter's http package does not support body in delete method
    @PostMapping("/delete/{itemId}")
    @ResponseStatus(HttpStatus.FOUND)
    fun deleteItem(@Valid @RequestBody baseClientDto: BaseClientDto, @PathVariable itemId: Long) =
            deleteItemFromCart.delete(baseClientDto.phoneNumber, baseClientDto.key, itemId)

    @PostMapping("/delete")
    @ResponseStatus(HttpStatus.FOUND)
    fun delete(@Valid @RequestBody dto: BaseClientDto) = deleteCart.delete(dto.phoneNumber, dto.key)
}
