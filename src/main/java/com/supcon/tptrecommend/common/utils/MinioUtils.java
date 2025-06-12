package com.supcon.tptrecommend.common.utils;

import com.supcon.systemcommon.exception.ServerException;
import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
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
        if(objectName.endsWith("/")){
            deleteFolder(bucketName, objectName);
            return;
        }
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
     * 删除文件夹
     * @param bucketName 存储桶名称
     * @param folderPath 文件夹路径
     * @author luhao
     * @since 2025/06/12 15:17:28
     */
    public void deleteFolder(String bucketName, String folderPath)  {
        Iterable<Result<Item>> objects = minioClient.listObjects(
            ListObjectsArgs.builder()
                .bucket(bucketName)
                .prefix(folderPath)
                .recursive(true)
                .build());

        // 2. 将所有对象的名称收集起来，准备批量删除
        List<DeleteObject> objectsToDelete = new LinkedList<>();
        for (Result<Item> result : objects) {
            try {
                String objectName = result.get().objectName();
                objectsToDelete.add(new DeleteObject(objectName));
            } catch (Exception e) {
                // 处理获取对象时的异常
                log.error("获取对象信息时出错: ", e);
            }
        }

        // 如果列表为空，说明没有文件需要删除，直接返回
        if (objectsToDelete.isEmpty()) {
            return;
        }

        // 3. 执行批量删除操作
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(
            RemoveObjectsArgs.builder()
                .bucket(bucketName)
                .objects(objectsToDelete)
                .build());

        // 检查删除结果
        for (Result<DeleteError> result : results) {
            try {
                DeleteError error = result.get();
                log.error("删除文件:{},时出错:{} ", error.objectName(), error);
            } catch (Exception e) {
                // 处理获取删除结果时的异常
                log.error("检查删除结果时出错: ", e);
            }
        }
        log.info("成功删除文件夹 '{}' 下的所有文件。", folderPath);
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
            log.error("文件获取失败: ", e);
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

    /**
     * 创建文件夹
     *
     * @param bucketName 存储桶名称
     * @param folderName 文件夹名称
     * @author luhao
     * @since 2025/06/11 16:30:11
     */
    public void createFolder(String bucketName, String folderName) {
        try {
            // 确保 folderName 以斜杠结尾
            if (!folderName.endsWith("/")) {
                folderName += "/";
            }

            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(folderName)  // 注意：以 / 结尾
                    .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                    .contentType("application/x-directory")  // 可选，用于表示这是目录
                    .build()
            );

        } catch (Exception e) {
            log.error("创建文件夹:{}失败: ", folderName, e);
        }
    }

    public Iterable<Result<Item>> listObjects(String bucketName, String path) {
        // 使用 listObjects 来获取文件和文件夹
        return minioClient.listObjects(
            ListObjectsArgs.builder()
                .bucket(bucketName)
                .prefix(path) // 设置查询的前缀（即当前“目录”）
                .delimiter("/") // 使用'/'作为分隔符来模拟文件夹
                .build());
    }


}
