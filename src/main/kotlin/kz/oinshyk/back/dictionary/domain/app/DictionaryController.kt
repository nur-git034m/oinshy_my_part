package kz.oinshyk.back.dictionary.domain.app

import kz.oinshyk.back.common.app.ApiController
import kz.oinshyk.back.dictionary.domain.port.CityRepository
import org.springframework.data.rest.webmvc.BasePathAwareController
import org.springframework.web.bind.annotation.GetMapping

@ApiController("dictionary")
@BasePathAwareController
class DictionaryController(
        private val cityRepository: CityRepository
) {

    @GetMapping("cities")
    fun getCities() = cityRepository.findAll()
}
