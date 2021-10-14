package kz.oinshyk.back.dictionary.domain.app

import kz.oinshyk.back.common.app.ApiController
import kz.oinshyk.back.dictionary.domain.service.GetSubscriptionPrice
import kz.oinshyk.back.dictionary.domain.usecase.GetDeliveryPrice
import org.springframework.data.rest.webmvc.BasePathAwareController
import org.springframework.web.bind.annotation.GetMapping

@ApiController("params")
@BasePathAwareController
class GlobalParamsController(
        private val getDeliveryPrice: GetDeliveryPrice,
        private val getSubscriptionPrice: GetSubscriptionPrice
) {

    @GetMapping("delivery-price")
    fun deliveryPrice() = getDeliveryPrice.price()

    @GetMapping("subscription-price")
    fun subscriptionPrice() = getSubscriptionPrice.price()
}
