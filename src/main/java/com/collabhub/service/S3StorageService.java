package com.collabhub.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.UUID;

@Service
@Profile("prod")
public class S3StorageService implements StorageService {

    private final S3Client    s3Client;
    private final S3Presigner presigner;
    private final String      bucketName;

    public S3StorageService(
            S3Client s3Client,
            S3Presigner presigner,
            @Value("${app.s3.bucket}") String bucketName) {
        this.s3Client   = s3Client;
        this.presigner  = presigner;
        this.bucketName = bucketName;
    }

    @Override
    public String store(MultipartFile file) throws IOException {
        String extension = "";
        String original  = file.getOriginalFilename();
        if (original != null && original.contains(".")) {
            extension = original.substring(original.lastIndexOf('.'));
        }
        String key = "attachments/" + UUID.randomUUID() + extension;

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .contentType(file.getContentType())
                        .build(),
                RequestBody.fromInputStream(file.getInputStream(), file.getSize())
        );
        return key;
    }

    @Override
    public InputStream load(String storedFileName) {
        return s3Client.getObject(
                GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(storedFileName)
                        .build()
        );
    }

    @Override
    public void delete(String storedFileName) {
        s3Client.deleteObject(
                DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(storedFileName)
                        .build()
        );
    }

    /**
     * Pre-signed URL — expires in 15 minutes.
     * Use when serving files to the frontend instead of load().
     */
    public String generatePresignedUrl(String key) {
        var presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15))
                .getObjectRequest(r -> r.bucket(bucketName).key(key))
                .build();
        return presigner.presignGetObject(presignRequest).url().toString();
    }
}
