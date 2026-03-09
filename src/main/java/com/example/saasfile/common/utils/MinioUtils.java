package com.example.saasfile.common.utils;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.example.saasfile.support.exception.ClientException;
import com.example.saasfile.support.exception.ServerException;
import io.minio.*;
import io.minio.messages.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Component
@Configuration
@RequiredArgsConstructor
public class MinioUtils {

    
    @Value("${minio.endpoint}")
    private String endpoint;
    
    @Value("${minio.accessKey}")
    private String accessKey;
    
    @Value("${minio.secretKey}")
    private String secretKey;

    private MinioClient minioClient;
    private MultipartEnabledMinioClient multipartMinioClient;

    @Value("${file.temp-dir:D:/temp/uploads}")
    private String tempDir;

    @PostConstruct
    public void initClient() {
        this.minioClient = MinioClient.builder()
            .endpoint(endpoint)
            .credentials(accessKey, secretKey)
            .build();
        this.multipartMinioClient = new MultipartEnabledMinioClient(this.minioClient);
    }


    
    public boolean bucketExists(String bucketName) {
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            return false;
        }
    }

    
    public void makeBucket(String bucketName) {
        try {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            log.info("Bucket created: " + bucketName);
        } catch (Exception e) {
            log.error("Error creating bucket: " + e);
        }
    }

    
    public void uploadFile(String bucketName, String objectName, InputStream inputStream, String contentType, long size) throws Exception {
        if (!bucketExists(bucketName)) {
            makeBucket(bucketName);
        }
        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .stream(inputStream, size, -1)
                .contentType(contentType)
                .build());


    }

    
    public void uploadToMinio(String bucketName, String objectName, Path filePath) {
        try (InputStream inputStream = Files.newInputStream(filePath)) {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, inputStream.available(), -1)
                    .build()
            );
            log.info("Uploaded object to MinIO: {}", objectName);
        } catch (Exception e) {
            log.error("Upload to MinIO failed. object={}", objectName, e);
        }
    }

    
    public void removeFile(String bucketName, String objectName) {
        if (objectName.endsWith("/")) {
            deleteFolder(bucketName, objectName);
            return;
        }
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build());
        } catch (Exception e) {
            throw new ServerException("delete file failed", e);
        }

    }

    
    public void deleteFolder(String bucketName, String folderPath) {
        Iterable<Result<Item>> objects = minioClient.listObjects(
            ListObjectsArgs.builder()
                .bucket(bucketName)
                .prefix(folderPath)
                .recursive(true)
                .build());
        List<DeleteObject> objectsToDelete = new LinkedList<>();
        for (Result<Item> result : objects) {
            try {
                String objectName = result.get().objectName();
                objectsToDelete.add(new DeleteObject(objectName));
            } catch (Exception e) {
                log.error("Failed to list object while deleting folder", e);
            }
        }
        if (objectsToDelete.isEmpty()) {
            return;
        }
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(
            RemoveObjectsArgs.builder()
                .bucket(bucketName)
                .objects(objectsToDelete)
                .build());
        for (Result<DeleteError> result : results) {
            try {
                DeleteError error = result.get();
                log.error("Delete folder object failed. object={}, error={}", error.objectName(), error);
            } catch (Exception e) {
                log.error("Delete folder failed", e);
                throw new ServerException("delete folder failed");
            }
        }
        log.info("Deleted all objects under folder: {}", folderPath);
    }

    
    public void removeFiles(String bucketName, List<String> objectNames) {
        List<String> folders = objectNames.stream().filter(s -> s.endsWith("/")).collect(Collectors.toList());
        if (!folders.isEmpty()) {
            for (String folder : folders) {
                deleteFolder(bucketName, folder);
            }
        }
        objectNames.removeAll(folders);
        if (objectNames.isEmpty()) {
            return;
        }
        List<DeleteObject> objectsToDelete = objectNames.stream()
            .map(DeleteObject::new)
            .collect(Collectors.toList());
        try {
            minioClient.removeObjects(
                    RemoveObjectsArgs.builder()
                        .bucket(bucketName)
                        .objects(objectsToDelete)
                        .build())
                .forEach(result -> {
                    try {
                        result.get();
                    } catch (Exception e) {
                        log.error("Batch delete object failed", e);
                    }
                });

        } catch (Exception e) {
            throw new ServerException("delete file failed", e);
        }

    }


    
    @SneakyThrows
    public List<String> getAllObjectItemByPrefix(String bucketName, String prefix, boolean recursive) {
        List<String> list = new ArrayList<>();
        Iterable<Result<Item>> objectsIterator = minioClient.listObjects(
            ListObjectsArgs.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .recursive(recursive)
                .build());
        if (objectsIterator != null) {
            for (Result<Item> result : objectsIterator) {
                Item item = result.get();
                list.add(item.objectName());
            }
        }
        return list;
    }

    
    public Iterable<Result<Item>> listObjects(String bucketName, String prefix) {
        return minioClient.listObjects(ListObjectsArgs.builder()
            .bucket(bucketName)
            .prefix(prefix)
            .recursive(true)
            .build());

    }


    
    public String getPresignedPutUrl(String bucketName, String objectName, int expirySeconds) {
        try {
            if (!bucketExists(bucketName)) {
                makeBucket(bucketName);
            }
            return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(io.minio.http.Method.PUT)
                    .bucket(bucketName)
                    .object(objectName)
                    .expiry(expirySeconds)
                    .build()
            );
        } catch (Exception e) {
            log.error("Failed to create presigned PUT url. bucket={}, object={}", bucketName, objectName, e);
            throw new ServerException("create presigned URL failed", e);
        }
    }

    public String createMultipartUpload(String bucketName, String objectName, String contentType) {
        try {
            if (!bucketExists(bucketName)) {
                makeBucket(bucketName);
            }
            Multimap<String, String> headers = HashMultimap.create();
            if (contentType != null && !contentType.trim().isEmpty()) {
                headers.put("Content-Type", contentType.trim());
            }
            CreateMultipartUploadResponse response = multipartMinioClient
                .createMultipartUploadInternal(bucketName, null, objectName, headers, null);
            return response.result().uploadId();
        } catch (Exception e) {
            log.error("Create multipart upload failed. bucket={}, object={}", bucketName, objectName, e);
            throw new ServerException("Create multipart upload failed", e);
        }
    }

    public String getPresignedUploadPartUrl(String bucketName,
                                            String objectName,
                                            String uploadId,
                                            int partNumber,
                                            int expirySeconds) {
        try {
            if (!bucketExists(bucketName)) {
                makeBucket(bucketName);
            }
            Map<String, String> queryParams = new HashMap<>(2);
            queryParams.put("uploadId", uploadId);
            queryParams.put("partNumber", String.valueOf(partNumber));
            return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(io.minio.http.Method.PUT)
                    .bucket(bucketName)
                    .object(objectName)
                    .expiry(expirySeconds)
                    .extraQueryParams(queryParams)
                    .build()
            );
        } catch (Exception e) {
            log.error("Failed to create presigned multipart url. bucket={}, object={}, part={}",
                bucketName, objectName, partNumber, e);
            throw new ServerException("Create multipart presigned url failed", e);
        }
    }

    public void completeMultipartUpload(String bucketName, String objectName, String uploadId, long expectedSize) {
        try {
            List<Part> uploadedParts = listAllUploadedParts(bucketName, objectName, uploadId);
            if (uploadedParts.isEmpty()) {
                throw new ClientException("No uploaded multipart parts found");
            }

            long totalUploadedSize = uploadedParts.stream().mapToLong(Part::partSize).sum();
            if (totalUploadedSize != expectedSize) {
                throw new ClientException("Uploaded file size mismatch");
            }

            multipartMinioClient.completeMultipartUploadInternal(
                bucketName,
                null,
                objectName,
                uploadId,
                uploadedParts.toArray(new Part[0]),
                null,
                null
            );
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            log.error("Complete multipart upload failed. bucket={}, object={}, uploadId={}",
                bucketName, objectName, uploadId, e);
            throw new ServerException("Complete multipart upload failed", e);
        }
    }

    private List<Part> listAllUploadedParts(String bucketName, String objectName, String uploadId) throws Exception {
        List<Part> parts = new ArrayList<>();
        int partNumberMarker = 0;
        while (true) {
            ListPartsResponse response = multipartMinioClient.listPartsInternal(
                bucketName,
                null,
                objectName,
                1000,
                partNumberMarker,
                uploadId,
                null,
                null
            );
            ListPartsResult result = response.result();
            if (result == null || result.partList() == null || result.partList().isEmpty()) {
                break;
            }
            List<Part> page = result.partList();
            parts.addAll(page);
            if (!result.isTruncated()) {
                break;
            }
            partNumberMarker = page.get(page.size() - 1).partNumber();
        }
        return parts;
    }

    public void composeObject(String bucketName, String targetObjectName, List<String> sourceObjectNames) {
        if (sourceObjectNames == null || sourceObjectNames.isEmpty()) {
            throw new ClientException("source object list cannot be empty");
        }
        try {
            if (!bucketExists(bucketName)) {
                makeBucket(bucketName);
            }
            List<ComposeSource> sources = sourceObjectNames.stream()
                .map(source -> ComposeSource.builder().bucket(bucketName).object(source).build())
                .collect(Collectors.toList());
            minioClient.composeObject(
                ComposeObjectArgs.builder()
                    .bucket(bucketName)
                    .object(targetObjectName)
                    .sources(sources)
                    .build()
            );
        } catch (Exception e) {
            log.error("Compose object failed. bucket={}, target={}", bucketName, targetObjectName, e);
            throw new ServerException("compose object failed", e);
        }
    }

    public InputStream getFileInputStream(String bucketName, String objectName) {
        try {
            return minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.error("Failed to fetch object from MinIO. object={}", objectName, e);
            throw new ServerException("get object failed", e);
        }


    }

    
    public StatObjectResponse getMetadata(String bucketName, String objectName) {
        try {
            return minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            throw new ClientException("file not found");
        }
    }

    
    public void createFolder(String bucketName, String folderName) {
        if (!bucketExists(bucketName)) {
            makeBucket(bucketName);
        }
        try {
            if (!folderName.endsWith("/")) {
                folderName += "/";
            }

            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(folderName)
                    .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                    .contentType("application/x-directory")
                    .build()
            );

        } catch (Exception e) {
            log.error("Create folder failed. folder={}", folderName, e);
            throw new ServerException("create folder failed");

        }
    }


    
    public int countFilePrefix(String bucketName, String prefix) {
        if (!prefix.endsWith("/")) {
            prefix += "/";
        }
        Iterable<Result<Item>> objectsIterator = minioClient.listObjects(
            ListObjectsArgs.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .recursive(true)
                .build());
        int count = 0;
        for (Result<Item> result : objectsIterator) {
            try {
                Item item = result.get();
                if (item.size() == 0) {
                    continue;
                }
                if (!item.isDir()) {
                    count++;
                }
            } catch (Exception e) {
                log.error("Failed to count objects under prefix: {}", prefix, e);
            }
        }
        return count;
    }

    
    public File saveStreamToTempFile(String bucketName, String objectName, String uniqueFilename) {
        Path destinationPath = Paths.get(tempDir, uniqueFilename);
        Path parentDir = destinationPath.getParent();
        if (Files.notExists(parentDir)) {
            try {
                Files.createDirectories(parentDir);
            } catch (IOException e) {
                log.error("Failed to create temp directory: {}", parentDir, e);
                return null;
            }
        }
        try (InputStream stream = getFileInputStream(bucketName, objectName)) {
            Files.copy(stream, destinationPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            log.error("Failed to fetch object from MinIO", e);
            return null;
        }
        return destinationPath.toFile();
    }

    private static class MultipartEnabledMinioClient extends MinioClient {

        MultipartEnabledMinioClient(MinioClient minioClient) {
            super(minioClient);
        }

        CreateMultipartUploadResponse createMultipartUploadInternal(String bucketName,
                                                                    String region,
                                                                    String objectName,
                                                                    Multimap<String, String> extraHeaders,
                                                                    Multimap<String, String> extraQueryParams) throws Exception {
            return super.createMultipartUpload(bucketName, region, objectName, extraHeaders, extraQueryParams);
        }

        ListPartsResponse listPartsInternal(String bucketName,
                                            String region,
                                            String objectName,
                                            Integer maxParts,
                                            Integer partNumberMarker,
                                            String uploadId,
                                            Multimap<String, String> extraHeaders,
                                            Multimap<String, String> extraQueryParams) throws Exception {
            return super.listParts(bucketName, region, objectName, maxParts, partNumberMarker, uploadId, extraHeaders, extraQueryParams);
        }

        void completeMultipartUploadInternal(String bucketName,
                                             String region,
                                             String objectName,
                                             String uploadId,
                                             Part[] parts,
                                             Multimap<String, String> extraHeaders,
                                             Multimap<String, String> extraQueryParams) throws Exception {
            super.completeMultipartUpload(bucketName, region, objectName, uploadId, parts, extraHeaders, extraQueryParams);
        }
    }



}

