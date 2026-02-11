package com.supcon.tptrecommend.common.utils;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.supcon.systemcommon.exception.ClientException;
import com.supcon.systemcommon.exception.ServerException;
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

/**
 * <p>
 * minio鐎规悶鍎遍崣璺ㄧ尵?
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
     * minio闁革附婢樺?缂佹棏鍨拌ぐ娑㈠矗?
     */
    @Value("${minio.endpoint}")
    private String endpoint;
    /**
     * minio闁活潿鍔嶉崺娑㈠触?
     */
    @Value("${minio.accessKey}")
    private String accessKey;
    /**
     * minio閻庨潧妫涢悥?
     */
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


    /**
     * 婵☆偀鍋撻柡灞诲劜閵嗗﹪寮伴姘剨閻庢稒锚濠€?
     *
     * @param bucketName 閻庢稒锚閸嬪秴顩肩捄鐑樺€崇紒?
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
     * 闁告帗绋戠紓鎾愁浖?
     *
     * @param bucketName 閻庢稒锚閸嬪秴顩肩捄鐑樺€崇紒?
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
     * 濞戞挸锕ｇ槐鍫曞棘閸ワ附顐介柛?MinIO
     *
     * @param bucketName  閻庢稒锚閸嬪秴顩肩捄鐑樺€崇紒?
     * @param objectName  閻庣數顢婇挅鍕触瀹ュ泦鐐烘晬閸喐鐎ù鐘烘硾濠€?MinIO 濞戞搩鍘惧▓鎴犳崉椤栨氨绐為柨?
     * @param inputStream 闁哄倸娲ｅ▎銏℃綇閹惧啿寮虫繛?
     * @param contentType 闁哄倸娲ｅ▎銏㈢尵鐠囪尙鈧?
     * @param size        濠㈠爢鍐瘓
     * @throws Exception 鐎殿喖鍊搁悥?
     * @author luhao
     * @since 2025/08/21 19:05:54
     */
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

    /**
     * 闂侇偅淇虹换鍐棘閸ワ附顐介悹渚灠缁剁偞绋夋繝浣虹倞闁哄倸娲ｅ▎銏ゅ礆?MinIO
     *
     * @param bucketName 閻庢稒锚閸嬪秴顩肩捄鐑樺€崇紒?
     * @param objectName 閻庣數顢婇挅鍕触瀹ュ泦鐐烘晬閸喐鐎ù鐘烘硾濠€?MinIO 濞戞搩鍘惧▓鎴犳崉椤栨氨绐為柨?
     * @param filePath   闁哄倸娲ｅ▎銏㈡崉椤栨氨绐?
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
            log.info("闁哄倸娲ｅ▎銏＄▔婵犱胶鐐婇柟瀛樺姇婵? " + objectName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 闁告帞濞€濞呭酣寮崶锔筋偨
     *
     * @param bucketName 閻庢稒锚閸嬪秴顩肩捄鐑樺€崇紒?
     * @param objectName 閻庣數顢婇挅鍕触瀹ュ泦鐐烘晬閸喐鐎ù鐘烘硾濠€?MinIO 濞戞搩鍘惧▓鎴犳崉椤栨氨绐為柨?
     */
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

    /**
     * 闁告帞濞€濞呭酣寮崶锔筋偨濠?
     *
     * @param bucketName 閻庢稒锚閸嬪秴顩肩捄鐑樺€崇紒?
     * @param folderPath 闁哄倸娲ｅ▎銏″緞绾懐鐔呯€?
     * @author luhao
     * @since 2025/06/12 15:17:28
     */
    public void deleteFolder(String bucketName, String folderPath) {
        Iterable<Result<Item>> objects = minioClient.listObjects(
            ListObjectsArgs.builder()
                .bucket(bucketName)
                .prefix(folderPath)
                .recursive(true)
                .build());

        // 2. 閻忓繐妫欐晶宥夊嫉婢跺鍤犻悹鐑囩磿濞堟垿宕ュ鍥嗙偤寮ㄩ崼鏇熻偁閻犙囨敱濞肩敻鏁嶇仦钘夋珯濠㈣泛娲︽竟鎺楁煂韫囨挸鐏╅梻?
        List<DeleteObject> objectsToDelete = new LinkedList<>();
        for (Result<Item> result : objects) {
            try {
                String objectName = result.get().objectName();
                objectsToDelete.add(new DeleteObject(objectName));
            } catch (Exception e) {
                // 濠㈣泛瀚幃濠囨嚔瀹勬澘绲块悗鐢殿攰閽栧嫰寮崜浣圭暠鐎殿喖鍊搁悥?
                log.error("闁兼儳鍢茶ぐ鍥┾偓鐢殿攰閽栧嫭绌遍埄鍐х礀闁哄啳娉涢崵顓㈡煥? ", e);
            }
        }

        // 濠碘€冲€归悘澶愬礆濡ゅ嫨鈧啯绋夐搹鍏夋晞闁挎稑鐭侀鈺呭及鎼淬垻姊鹃柡鍫濐槹閺嬪啯绂掗崼鏇熶粯閻熸洑绀侀崹褰掓⒔閵堝繒绀夐柣鈺佺摠鐢瓨娼婚弬鎸庣
        if (objectsToDelete.isEmpty()) {
            return;
        }

        // 3. 闁圭瑳鍡╂斀闁归潧缍婇崳娲礆閻樼粯鐝熼柟鍨С缂?
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(
            RemoveObjectsArgs.builder()
                .bucket(bucketName)
                .objects(objectsToDelete)
                .build());

        // 婵☆偀鍋撻柡灞诲劚閸ㄥ綊姊介妶鍥╂尝闁?
        for (Result<DeleteError> result : results) {
            try {
                DeleteError error = result.get();
                log.error("闁告帞濞€濞呭酣寮崶锔筋偨:{},闁哄啳娉涢崵顓㈡煥?{} ", error.objectName(), error);
            } catch (Exception e) {
                // 濠㈣泛瀚幃濠囨嚔瀹勬澘绲块柛鎺斿█濞呭海绱掗幘瀵镐函闁哄啫澧庡▓鎴濐嚕閸屾氨鍩?
                log.error("婵☆偀鍋撻柡灞诲劚閸ㄥ綊姊介妶鍥╂尝闁哄绮嶅鍌炲礄濞差亝鏅? ", e);
                throw new ServerException("delete folder failed");
            }
        }
        log.info("Deleted all objects under folder: {}", folderPath);
    }

    /**
     * 闁归潧缍婇崳娲礆閻樼粯鐝熼柡鍌氭矗濞?
     *
     * @param bucketName  閻庢稒锚閸嬪秴顩肩捄鐑樺€崇紒?
     * @param objectNames 閻庣數顢婇挅鍕触瀹ュ泦?
     * @author luhao
     * @date 2025/06/04 19:35:28
     */
    public void removeFiles(String bucketName, List<String> objectNames) {
        //  闁告帞濞€濞呭酣寮崶锔筋偨濠?
        List<String> folders = objectNames.stream().filter(s -> s.endsWith("/")).collect(Collectors.toList());
        if (!folders.isEmpty()) {
            for (String folder : folders) {
                deleteFolder(bucketName, folder);
            }
        }
        // 缂佸顭峰▍搴ㄥ棘閸ワ附顐藉璺烘贡濞叉媽銇?
        objectNames.removeAll(folders);
        if (objectNames.isEmpty()) {
            return;
        }
        // 闁告帞濞€濞呭酣寮崶锔筋偨
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
                        result.get();  // get() 闁硅埖绋戦崵顓烆嚕閸屾氨鍩楀ù鐙呯秬閵嗗啴宕氶悩缁樼彑濠㈡儼绮剧憴?
                    } catch (Exception e) {
                        log.error("minio闁哄倸娲ｅ▎銏ゅ礆閻樼粯鐝熷鎯扮簿鐟? ", e);
                    }
                });

        } catch (Exception e) {
            throw new ServerException("delete file failed", e);
        }

    }


    /**
     * 闁哄秷顫夊畵渚€寮崶锔筋偨闁告挸绉剁槐鎴﹀蓟閵夘煈鍤勯柡鍌氭矗濞?
     *
     * @param bucketName bucket闁告艾绉惰ⅷ
     * @param prefix     闁告挸绉剁槐?
     * @param recursive  闁哄嫷鍨伴幆渚€鏌呴幒鎴犵Ш闁哄被鍎撮?
     * @return MinioItem 闁告帗顨夐妴?
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
     * 闁哄秷顫夊畵渚€宕滃鍥╃；闁告帗顨呴崵顓犫偓鐢殿攰閽?
     *
     * @param bucketName 閻庢稒锚閸嬪秴顩肩捄鐑樺€崇紒?
     * @param prefix     闁告挸绉剁槐?
     * @return {@link Iterable }<{@link Result }<{@link Item }>>
     * @author luhao
     * @since 2025/10/30 13:31:47
     *
     */
    public Iterable<Result<Item>> listObjects(String bucketName, String prefix) {
        return minioClient.listObjects(ListObjectsArgs.builder()
            .bucket(bucketName)
            .prefix(prefix)
            .recursive(true)
            .build());

    }


    /**
     * 闁兼儳鍢茶ぐ鍥棘閸ワ附顐介悗娑欘殙婵?
     *
     * @param bucketName 閻庢稒锚閸嬪秴顩肩捄鐑樺€崇紒?
     * @param objectName 閻庣數顢婇挅鍕触瀹ュ泦?
     * @return {@link InputStream }
     * @author luhao
     * @date 2025/05/29 16:16:21
     */
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
            log.error("闁哄倸娲ｅ▎銏ゆ嚔瀹勬澘绲垮鎯扮簿鐟? ", e);
            throw new ServerException("get object failed", e);
        }


    }

    /**
     * 闁兼儳鍢茶ぐ鍥礂閸愨晜娈堕柟?
     *
     * @param bucketName 閻庢稒锚閸嬪秴顩肩捄鐑樺€崇紒?
     * @param objectName 閻庣數顢婇挅鍕触瀹ュ泦?
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
            throw new ClientException("file not found");
        }
    }

    /**
     * 闁告帗绋戠紓鎾诲棘閸ワ附顐藉?
     *
     * @param bucketName 閻庢稒锚閸嬪秴顩肩捄鐑樺€崇紒?
     * @param folderName 闁哄倸娲ｅ▎銏″緞閻熺増鍊崇紒?
     * @author luhao
     * @since 2025/06/11 16:30:11
     */
    public void createFolder(String bucketName, String folderName) {
        if (!bucketExists(bucketName)) {
            makeBucket(bucketName);
        }
        try {
            // 缁绢収鍠曠换?folderName 濞寸姰鍎查弸鈺呭级閻樼數娉㈤悘?
            if (!folderName.endsWith("/")) {
                folderName += "/";
            }

            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(folderName)  // 婵炲鍔嶉崜浼存晬濮橆偂绨?/ 缂備焦鎸搁悢?
                    .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                    .contentType("application/x-directory")  // 闁告瑯鍨堕埀顒€顧€缁辨繈鎮介妸銈囪壘閻炴稏鍔庨妵姘交濞嗘劖笑闁烩晩鍠栫紞?
                    .build()
            );

        } catch (Exception e) {
            log.error("闁告帗绋戠紓鎾诲棘閸ワ附顐藉?{}濠㈡儼绮剧憴? ", folderName, e);
            throw new ServerException("create folder failed");

        }
    }


    /**
     * 濞寸姰鍎辨晶鐘电磽閳ь剙顕ｉ埀顒佹叏鐎ｎ剛鍩犻悹浣插墲閺嬪啯绂掗懜鍨闂?
     *
     * @param bucketName 閻庢稒锚閸嬪秴顩肩捄鐑樺€崇紒?
     * @param prefix     闁告挸绉剁槐?
     * @return int
     * @author luhao
     * @since 2025/06/12 18:43:29
     */
    public int countFilePrefix(String bucketName, String prefix) {
        // 缁绢収鍠曠换?folderName 濞寸姰鍎查弸鈺呭级閻樼數娉㈤悘?
        if (!prefix.endsWith("/")) {
            prefix += "/";
        }
        Iterable<Result<Item>> objectsIterator = minioClient.listObjects(
            ListObjectsArgs.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .recursive(true)
                .build());
        // 闁兼儳鍢茶ぐ鍣奲jectsIterator闁轰椒鍗抽崳?
        int count = 0;
        for (Result<Item> result : objectsIterator) {
            try {
                Item item = result.get();
                if (item.size() == 0) {
                    continue;
                }
                if (!item.isDir()) {
                    // 闂侇偄顦甸妴?: 濞寸姴鎳愮划铏规媼閳╁啯鐎ù鐘侯啇缁辨瑦绋夊鍛樁闁告凹鍋呴弸鍐╃鐠烘亽浠氶柨?
                    count++;
                }
            } catch (Exception e) {
                log.error("缂備胶鍠曢鍝モ偓鐢殿攰閽栧嫭寰勬潏顐バ? {}", e.getMessage());
            }
        }
        return count;
    }

    /**
     * 濞?MinIO 濡ゅ倹蓱閺呫儵宕锋０浣虹憮閺夌偠濮ら弸鍐╃鐠洪缚瀚欏ǎ鍥ㄧ箓閻°劑宕氶悧鍫熸嫳闁革箓顣︽径宥夊籍閸撲焦绐楃憸鐗堟磸閳?
     *
     * @param bucketName     婵?闁告艾绉惰ⅷ
     * @param objectName     閻庣數顢婇挅鍕晬閸喐鐎ù鐘侯啇缁辨岸宕ュ鍥?
     * @param uniqueFilename 闁哥儐鍨粩鎾棘閸ワ附顐介柛?
     * @return {@link File }
     * @author luhao
     * @since 2025/08/08 13:57:04
     *
     */
    public File saveStreamToTempFile(String bucketName, String objectName, String uniqueFilename) {
        // 1. 闁哄瀚紓鎾垛偓鐟版湰閺嗭綁鎯冮崟顓熺獥闁哄秴娲﹂弸鍐╃閹壆鐔呯€?
        Path destinationPath = Paths.get(tempDir, uniqueFilename);
        Path parentDir = destinationPath.getParent();
        // 2. 缁绢収鍠曠换姘舵偉閸撲焦绐楃憸鐗堟礀閻°劑宕烽…鎺旂濠碘€冲€归悘澶嬬▔瀹ュ懐鎽犻柛锔哄妼閸垶宕氬☉妯肩处
        if (Files.notExists(parentDir)) {
            try {
                Files.createDirectories(parentDir);
            } catch (IOException e) {
                log.error("闁告帗绋戠紓鎾绘儎椤旇偐绉垮鎯扮簿鐟? ", e);
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

