package kz.oinshyk.back.client.app.web

import kz.oinshyk.back.client.domain.service.FindSubscription
import kz.oinshyk.back.common.app.ApiController
import org.springframework.data.rest.webmvc.BasePathAwareController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@ApiController("subscription")
@BasePathAwareController
class SubscriptionController(
        private val findSubscription: FindSubscription
) {

    @GetMapping("{phoneNumber}/{key}")
    fun get(@PathVariable phoneNumber: String, @PathVariable key: String) = findSubscription.find(phoneNumber, key)
}
