package kz.oinshyk.back.catalog.app

import kz.oinshyk.back.catalog.infra.FileManager
import kz.oinshyk.back.common.app.ApiController
import org.springframework.data.rest.webmvc.BasePathAwareController
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile

@ApiController("file-manager")
@BasePathAwareController
@PreAuthorize("hasRole('ADMIN')")
class FileManagerController(private val fileManager: FileManager) {

    @PostMapping("category-image/{id}")
    fun uploadCategoryImage(@PathVariable id: Long, @RequestParam file: MultipartFile) = fileManager.uploadCategoryImage(id, file)

    @DeleteMapping("category-image/{id}")
    fun deleteCategoryImage(@PathVariable id: Long) = fileManager.deleteCategoryImage(id)

    @PostMapping("toy-image/{id}")
    fun uploadToyImage(@PathVariable id: Long, @RequestParam file: MultipartFile) = fileManager.uploadToyImage(id, file)

    @DeleteMapping("toy-image/{id}")
    fun deleteToyImage(@PathVariable id: Long) = fileManager.deleteToyImage(id)
}
