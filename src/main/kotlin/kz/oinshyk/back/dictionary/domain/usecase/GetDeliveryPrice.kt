package kz.oinshyk.back.dictionary.domain.usecase

import kz.oinshyk.back.dictionary.domain.port.SettingRepository
import kz.oinshyk.back.security.infra.runWithRole
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.ResponseStatus
import java.math.BigDecimal

@Service
class GetDeliveryPrice(
        private val settingRepository: SettingRepository
) {
    private val key = "delivery-price"

    fun price(): BigDecimal {
        val setting = runWithRole("ADMIN") {
            settingRepository.findByKey(key) ?: throw DeliveryPriceNotFound()
        }
        return BigDecimal(setting.value).setScale(2)
    }
}

@ResponseStatus(HttpStatus.PRECONDITION_FAILED)
class DeliveryPriceNotFound : RuntimeException()
