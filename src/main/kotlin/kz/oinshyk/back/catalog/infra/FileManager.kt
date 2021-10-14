package kz.oinshyk.back.catalog.infra

import kz.oinshyk.back.AppConfig
import kz.oinshyk.back.cart.domain.usecase.ToyNotFoundException
import kz.oinshyk.back.catalog.domain.entity.Category
import kz.oinshyk.back.catalog.domain.entity.Toy
import kz.oinshyk.back.catalog.domain.entity.ToyImage
import kz.oinshyk.back.catalog.domain.port.CategoryRepository
import kz.oinshyk.back.catalog.domain.port.ToyImageRepository
import kz.oinshyk.back.catalog.domain.port.ToyRepository
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest

@Service
@PreAuthorize("hasRole('ADMIN')")
class FileManager(
        private val appConfig: AppConfig,
        private val config: S3StorageConfig,
        private val s3Client: S3Client,
        private val categoryRepository: CategoryRepository,
        private val toyRepository: ToyRepository,
        private val toyImageRepository: ToyImageRepository
) {

    private val categoriesDir = "category"
    private val toysDir = "toy"
    private val defaultCategoryImage = "logo.png"

    fun uploadCategoryImage(id: Long, file: MultipartFile) {
        val fileName = "$id-${file.originalFilename}"

        val category = categoryRepository.findById(id).orElseThrow { throw CategoryNotFoundException() }
        if (category.image != defaultCategoryImage) {
            delete(categoriesDir, category.image)
        }
        category.image = fileName
        categoryRepository.save(category)

        put(categoriesDir, fileName, file)
    }

    fun uploadToyImage(id: Long, file: MultipartFile) {
        val fileName = "$id-${file.originalFilename}"

        val toy = toyRepository.findById(id).orElseThrow { throw ToyNotFoundException() }
        val toyImage = ToyImage(fileName, toy)
        toyImageRepository.save(toyImage)

        put(toysDir, fileName, file)
    }

    private fun put(dir: String, fileName: String, file: MultipartFile) {
        val key = "${appConfig.env}/$dir/$fileName"
        val request = PutObjectRequest.builder()
                .bucket(config.bucket).key(key)
                .contentType(file.contentType)
                .build()
        s3Client.putObject(request, RequestBody.fromBytes(file.bytes))
    }

    fun deleteCategoryImage(id: Long) {
        val category = categoryRepository.findById(id).orElseThrow { throw CategoryNotFoundException() }

        deleteCategoryImage(category)

        category.image = defaultCategoryImage
        categoryRepository.save(category)
    }

    fun deleteCategoryImage(category: Category) {
        if (category.image != defaultCategoryImage) delete(categoriesDir, category.image)
    }

    fun deleteToyImages(toy: Toy) {
        toy.images.forEach {
            delete(toysDir, it.fileName)
        }
    }

    @Transactional
    fun deleteToyImage(id: Long) {
        val toyImage = toyImageRepository.findById(id).orElseThrow { throw ToyImageNotFoundException() }

        delete(toysDir, toyImage.fileName)

        toyImage.toy.images.remove(toyImage)
        toyImageRepository.delete(toyImage)
    }

    private fun delete(dir: String, fileName: String) {
        val key = "${appConfig.env}/$dir/$fileName"
        val request = DeleteObjectRequest.builder()
                .bucket(config.bucket).key(key)
                .build()
        s3Client.deleteObject(request)
    }
}

@ResponseStatus(HttpStatus.NOT_FOUND)
class ToyImageNotFoundException : RuntimeException() {

}

@ResponseStatus(HttpStatus.NOT_FOUND)
class CategoryNotFoundException : RuntimeException()
