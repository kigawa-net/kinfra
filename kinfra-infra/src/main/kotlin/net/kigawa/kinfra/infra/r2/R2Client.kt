package net.kigawa.kinfra.infra.r2

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import software.amazon.awssdk.services.s3.model.*
import java.net.URI

class R2Client(
    accountId: String,
    accessKey: String,
    secretKey: String,
) {

    private val endpoint = "https://$accountId.r2.cloudflarestorage.com"

    private val s3Client: S3Client by lazy {
        val creds = AwsBasicCredentials.create(accessKey, secretKey)
        val credentialsProvider = StaticCredentialsProvider.create(creds)

        val serviceConfig = S3Configuration.builder()
            // パススタイルアクセス (必須であったり推奨されたり)
            .pathStyleAccessEnabled(true)
            .build()

        S3Client.builder()
            .endpointOverride(URI.create(endpoint))
            .credentialsProvider(credentialsProvider)
            .region(Region.of("auto"))
            .serviceConfiguration(serviceConfig)
            .build()
    }

    fun listBuckets(): List<Bucket> {
        val resp = s3Client.listBuckets(ListBucketsRequest.builder().build())
        return resp.buckets()
    }

    fun listObjects(bucketName: String): List<S3Object> {
        val resp = s3Client.listObjectsV2(
            ListObjectsV2Request.builder()
                .bucket(bucketName)
                .build()
        )
        return resp.contents()
    }

    fun putObject(bucketName: String, key: String, data: ByteArray) {
        s3Client.putObject(
            PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build(),
            RequestBody.fromBytes(data)
        )
    }

    fun getObject(bucketName: String, key: String): ByteArray {
        val resp = s3Client.getObject(
            GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build()
        )
        return resp.readAllBytes()
    }

    fun close() {
        s3Client.close()
    }
}