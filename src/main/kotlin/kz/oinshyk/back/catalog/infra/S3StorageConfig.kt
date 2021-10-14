package kz.oinshyk.back.catalog.infra

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Bean
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import java.net.URI

@ConstructorBinding
@ConfigurationProperties("app.s3-storage")
data class S3StorageConfig(
        val endpoint: String,
        val accessKey: String,
        val secretKey: String,
        val bucket: String
) {
    @Bean
    fun s3client(): S3Client? {
        val credentials = AwsBasicCredentials.create(accessKey, secretKey)
        return S3Client
                .builder()
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.AWS_GLOBAL)
                .endpointOverride(URI(endpoint))
                .build()
    }
}
