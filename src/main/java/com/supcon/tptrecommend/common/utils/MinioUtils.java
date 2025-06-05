package com.supcon.tptrecommend.common.utils;

import com.supcon.systemcommon.exception.ServerException;
import io.minio.*;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * minio工具类
 * </p>
 *
 * @author pengwei
 * @date 2025/05/22 13:30:41
 */
@Slf4j
@Component
@Configuration
@RequiredArgsConstructor
public class MinioUtils {

    /**
     * minio地址+端口号
     */
    @Value("${minio.endpoint}")
    private String endpoint;
    /**
     * minio用户名
     */
    @Value("${minio.accessKey}")
    private String accessKey;
    /**
     * minio密码
     */
    @Value("${minio.secretKey}")
    private String secretKey;

    private MinioClient minioClient;


    @PostConstruct
    public void initClient() {
        this.minioClient = MinioClient.builder()
            .endpoint(endpoint)
            .credentials(accessKey, secretKey)
            .build();
    }


    /**
     * 检查桶是否存在
     *
     * @param bucketName 存储桶名称
     * @return boolean
     * @author luhao
     * @date 2025/05/22 14:17:35
     */
    public boolean bucketExists(String bucketName) {
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 创建桶
     *
     * @param bucketName 存储桶名称
     * @author luhao
     * @date 2025/05/22 14:20:41
     */
    public void makeBucket(String bucketName) {
        try {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            log.info("Bucket created: " + bucketName);
        } catch (Exception e) {
            log.error("Error creating bucket: " + e);
        }
    }

    /**
     * 上传文件到 MinIO
     *
     * @param bucketName  存储桶名称
     * @param objectName  对象名称（文件在 MinIO 中的路径）
     * @param inputStream 文件输入流
     * @param contentType 文件类型
     * @throws Exception 上传失败时抛出异常
     */
    public void uploadFile(String bucketName, String objectName, InputStream inputStream, String contentType) throws Exception {
        if (!bucketExists(bucketName)) {
            makeBucket(bucketName);
        }
        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .stream(inputStream, inputStream.available(), -1)
                .contentType(contentType)
                .build());


    }

    /**
     * 通过文件路径上传文件到 MinIO
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称（文件在 MinIO 中的路径）
     * @param filePath   文件路径
     */
    public void uploadToMinio(String bucketName, String objectName, Path filePath) {
        try (InputStream inputStream = Files.newInputStream(filePath)) {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, inputStream.available(), -1)
                    .build()
            );
            log.info("文件上传成功: " + objectName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除文件
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称（文件在 MinIO 中的路径）
     */
    public void removeFile(String bucketName, String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build());
        } catch (Exception e) {
            throw new ServerException("文件删除失败", e);
        }

    }

    /**
     * 批量删除文件
     *
     * @param bucketName  存储桶名称
     * @param objectNames 对象名称
     * @author luhao
     * @date 2025/06/04 19:35:28
     */
    public void removeFiles(String bucketName, List<String> objectNames) {
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
                        result.get();  // get() 抛出异常代表删除失败
                    } catch (Exception e) {
                        log.error("minio文件删除失败: ", e);
                    }
                });

        } catch (Exception e) {
            throw new ServerException("文件删除失败", e);
        }

    }


    /**
     * 根据文件前缀查询文件
     *
     * @param bucketName bucket名称
     * @param prefix     前缀
     * @param recursive  是否递归查询
     * @return MinioItem 列表
     */
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


    /**
     * 获取文件字节
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return {@link InputStream }
     * @author luhao
     * @date 2025/05/29 16:16:21
     */
    public InputStream getFileBytes(String bucketName, String objectName) {
        try {
            return minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            throw new ServerException("文件获取失败", e);
        }


    }

    /**
     * 获取元数据
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return {@link StatObjectResponse }
     * @author luhao
     * @date 2025/05/29 16:23:58
     */
    public StatObjectResponse getMetadata(String bucketName, String objectName) {
        try {
            return minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
