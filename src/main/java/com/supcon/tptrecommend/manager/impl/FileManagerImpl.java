package com.supcon.tptrecommend.manager.impl;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.supcon.framework.tenant.core.getter.TenantContext;
import com.supcon.system.base.entity.AutoIdEntity;
import com.supcon.systemcommon.entity.IDList;
import com.supcon.systemcommon.entity.SupRequestBody;
import com.supcon.systemcommon.exception.ServerException;
import com.supcon.systemcommon.exception.SupException;
import com.supcon.systemmanagerapi.dto.LoginInfoUserDTO;
import com.supcon.tptrecommend.common.FileAnalysisHandleFactory;
import com.supcon.tptrecommend.common.utils.LoginUserUtils;
import com.supcon.tptrecommend.common.utils.MinioUtils;
import com.supcon.tptrecommend.convert.fileobject.FileObjectConvert;
import com.supcon.tptrecommend.dto.fileobject.*;
import com.supcon.tptrecommend.entity.FileObject;
import com.supcon.tptrecommend.manager.FileManager;
import com.supcon.tptrecommend.manager.FileParseManager;
import com.supcon.tptrecommend.service.IFileObjectService;
import io.minio.StatObjectResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileManagerImpl implements FileManager {

    @Value("${minio.bucket}")
    private String bucket;

    public static final String FILE_SPLIT = "/";

    private final MinioUtils minioUtils;

    private final IFileObjectService fileObjectService;

    private final Executor EXECUTOR = new ThreadPoolExecutor(10, 20,
        1000L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(100),
        new ThreadPoolExecutor.AbortPolicy());

    private final FileParseManager fileParseManager;

    private final FileAnalysisHandleFactory fileAnalysisHandleFactory;


    /**
     * 上传文件
     *
     * @param file       文件
     * @param attributes 属性
     * @return {@link Boolean }
     * @author luhao
     * @date 2025/05/22 14:56:00
     */
    @SneakyThrows
    @Override
    public FileObjectResp upload(MultipartFile file, String attributes, String path) {
        // 2. 生成对象键 (Object Key)
        String originalFilename = file.getOriginalFilename() == null ? "unknown" : file.getOriginalFilename();
        // 3. 生成唯一文件名
        String uniqueFilename = UUID.fastUUID().toString().replace("-", "") + "_" + originalFilename;
        LoginInfoUserDTO user = LoginUserUtils.getLoginUserInfo();
        // 文件全路径
        String objectKey;
        if (StrUtil.isNotBlank(path)) {
            if (!path.endsWith(FILE_SPLIT)) {
                path += FILE_SPLIT;
            }
            objectKey = getPath(user) + path + uniqueFilename;
        } else {
            objectKey = getPath(user) + uniqueFilename;
        }
        // 5.上传文件到MinIO
        uploadToMinio(file, objectKey);
        // 保存文件元数据 到数据库
        Long fileId = saveMetadataToDB(file, user, objectKey, originalFilename);
        // TODO: 大对象内存溢出问题
        byte[] bytes = file.getBytes();
        if (StrUtil.isBlank(attributes)) {
            CompletableFuture.runAsync(() -> {
                fileAnalysisHandleFactory.getHandler(getFileSuffix(originalFilename))
                    .ifPresent(fileAnalysisHandle -> fileAnalysisHandle.handleFileAnalysis(bytes, fileId));
            }, EXECUTOR).exceptionally(throwable -> {
                log.error("发送给大模型文件解析失败", throwable);
                return null;
            });

        }
        return FileObjectConvert.INSTANCE.convert(fileObjectService.getById(fileId));
    }

    public String getFileSuffix(String originalFilename) {
        if (originalFilename == null) {
            return "";
        }

        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex == -1) {
            return ""; // 无后缀
        }

        return originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
    }

  /*  private void saveFileTemporary(MultipartFile file){
        try {
            String TEMP_UPLOAD_DIR = "D:/temp/uploads/";
            // 确保目录存在
            File uploadDir = new File(TEMP_UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 创建一个唯一的目标文件路径
            String originalFilename = file.getOriginalFilename();
            Path destinationFile = Paths.get(TEMP_UPLOAD_DIR, System.currentTimeMillis() + "-" + originalFilename);

            // 立即将文件保存到目标路径
            file.transferTo(destinationFile);

            // 将文件路径传递给异步方法
            asyncFileService.processFilePath(destinationFile);

        } catch (IOException e) {
        }
    }

    public void processFilePath(Path filePath) {
        // 异步方法根据路径读取和处理文件
        System.out.println("Processing file at path: " + filePath + " in thread: " + Thread.currentThread().getName());

        try (InputStream inputStream = Files.newInputStream(filePath)) {
            // ... 在这里处理文件流 ...
            // 模拟耗时操作
            Thread.sleep(5000);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            // 处理异常
        } finally {
            // 重要：处理完后，根据业务需要决定是否删除这个临时保存的文件
            try {
                Files.delete(filePath);
                System.out.println("Cleaned up file: " + filePath);
            } catch (IOException e) {
                // log a warning
            }
        }
        System.out.println("Finished processing file at path: " + filePath);
    }*/


    private void uploadToMinio(MultipartFile file, String objectKey) {
        try {
            minioUtils.uploadFile(bucket, objectKey, file.getInputStream(), file.getContentType());
        } catch (Exception e) {
            log.error("上传失败: {}", objectKey, e);
            throw new ServerException("文件上传失败");
        }
    }

    private Long saveMetadataToDB(MultipartFile file, LoginInfoUserDTO user, String objectKey, String originalFilename) {
        return fileObjectService.saveObj(FileObjectCreateReq.builder()
            .userId(user.getId())
            .userName(user.getUsername())
            .objectName(objectKey)
            .originalName(originalFilename)
            .bucketName(bucket)
            .contentType(file.getContentType())
            .fileSize(file.getSize())
            .build());
    }


    /**
     * 获取路径
     * 按照租户/用户名/文件的方式
     *
     * @param user 用户
     * @return {@link String }
     * @author luhao
     * @date 2025/05/22 14:11:08
     */
    public String getPath(LoginInfoUserDTO user) {
        String tenant = TenantContext.getCurrentTenant();
        try {
            return tenant + FILE_SPLIT + user.getUsername() + FILE_SPLIT;
        } catch (SupException exception) {
            return tenant + FILE_SPLIT + user.getUsername() + FILE_SPLIT;
        }


    }


    /**
     * 删除文件
     *
     * @param id 主键
     * @return {@link Boolean }
     * @author luhao
     * @date 2025/05/22 15:10:06
     */
    @Override
    public Boolean delete(Long id) {
        // 根据id和用户名查询文件
        FileObject fileObject = fileObjectService.getOne(Wrappers.<FileObject>lambdaQuery()
            .eq(AutoIdEntity::getId, id)
            .eq(FileObject::getUserName, LoginUserUtils.getLoginUserInfo().getUsername()));
        if (fileObject == null) {
            throw new ServerException("文件不存在");
        }
        minioUtils.removeFile(fileObject.getBucketName(), fileObject.getObjectName());
        fileObjectService.removeById(id);
        return true;
    }

    /**
     * 文件分页查询
     *
     * @param body 请求体
     * @return {@link IPage }<{@link FileObjectResp }>
     * @throws Exception 例外
     * @author luhao
     * @date 2025/05/22 15:35:13
     */
    public IPage<FileObjectResp> selectPage(SupRequestBody<Map<String, String>> body) throws Exception {
        body.getData().put("userName", LoginUserUtils.getLoginUserInfo().getUsername());
        Optional.ofNullable(LoginUserUtils.getLoginUserInfo().getId()).ifPresent(id -> {
            body.getData().put("userId", String.valueOf(LoginUserUtils.getLoginUserInfo().getId()));
        });
        IPage<FileObjectResp> convert = fileObjectService.pageAutoQuery(body).convert(FileObjectConvert.INSTANCE::convert);
        List<FileObjectResp> records = convert.getRecords();
        records = records.stream()
            .filter(fileObjectResp -> !fileObjectResp.getObjectName().endsWith("/"))
            .collect(Collectors.toList());
        convert.setRecords(records);
        return convert;
    }

    /**
     * 获取单个文件流
     *
     * @param req      请求体，包含文件路径等信息
     * @param response 响应对象，用于输出文件流
     * @throws IOException 当文件读取或网络传输发生错误时抛出此异常
     * @author luhao
     * @date 2025/05/29 17:24:04
     */
    @Override
    public void getOne(SingleFileQueryReq req, HttpServletResponse response) throws IOException {
        // 获取文件路径
        String path = req.getPath();
        // 从MinIO中获取文件字节流
        InputStream inputStream = minioUtils.getFileBytes(bucket, path);
        // 获取文件元数据
        StatObjectResponse metadata = minioUtils.getMetadata(bucket, path);
        // 设置响应内容类型为文件的Content-Type
        response.setContentType(metadata.contentType());
        // 提取并编码文件名，用于响应头
        String originFileName = path.substring(path.indexOf("_") + 1);
        // 对中文文件名进行URL编码
        String encodedFileName = URLEncoder.encode(originFileName, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
        // 设置响应头，指定文件以行内形式打开，并设置文件名
        response.setHeader("Content-disposition", "inline;filename*=UTF-8''" + encodedFileName);
        // 设置响应内容长度
        response.setContentLengthLong(metadata.size());
        // 把文件流复制到响应输出流
        IOUtils.copy(inputStream, response.getOutputStream());
        // 刷新响应缓冲区，确保文件流发送
        response.flushBuffer();
        // 关闭输入流，释放资源
        inputStream.close();
    }

    @Override
    public String convertToMarkdown(MultipartFile file) throws Exception {
        return fileParseManager.parseFileToMarkdown(file, true);

    }

    @Override
    public FileObjectResp detail(Long fileId) {
        FileObject fileObject = fileObjectService.getById(fileId);
        return FileObjectConvert.INSTANCE.convert(fileObject);

    }

    /**
     * 批量删除
     *
     * @param data 文件id
     * @return {@link Boolean }
     * @author luhao
     * @date 2025/06/04 19:44:32
     */
    @Override
    public Boolean batchDelete(IDList<Long> data) {
        List<Long> ids = data.getIds();
        List<FileObject> fileObjects = fileObjectService.listByIds(ids);
        List<String> objectNames = fileObjects.stream().map(FileObject::getObjectName).collect(Collectors.toList());
        minioUtils.removeFiles(bucket, objectNames);
        fileObjectService.removeBatchByIds(ids);
        return true;
    }

    @Override
    public boolean createFolder(CreateFolderReq data) {
        LoginInfoUserDTO user = LoginUserUtils.getLoginUserInfo();
        String folderName = data.getFolderName();
        // 确保 folderName 以斜杠结尾
        if (!folderName.endsWith(FILE_SPLIT)) {
            folderName += FILE_SPLIT;
        }
        String path = getPath(user) + folderName;
        minioUtils.createFolder(bucket, path);
        saveMetadataToDB(user, path);
        return true;
    }

    private void saveMetadataToDB(LoginInfoUserDTO user, String path) {
        fileObjectService.saveObj(FileObjectCreateReq.builder()
            .userId(user.getId())
            .userName(user.getUsername())
            .objectName(path)
            .bucketName(bucket)
            .build());
    }


    /**
     * 获取文件夹层级结构
     *
     * @param path 路径
     * @return {@link List }<{@link FileNodeResp }>
     * @author luhao
     * @since 2025/06/12 15:24:19
     */
    public List<FileNodeResp> listFiles(String path) {
        if (StrUtil.isBlank(path)) {
            path = getPath(LoginUserUtils.getLoginUserInfo());
        }
        List<FileObject> fileObjects = fileObjectService.list(Wrappers.<FileObject>lambdaQuery()
            .likeRight(FileObject::getObjectName, path));

        // 用于最终返回的列表
        List<FileNodeResp> fileNodes = new ArrayList<>();
        // 用于临时存储直接子文件夹的名称，利用Set自动去重
        Set<Folder> folderNames = new HashSet<>();
        // 用于记录已经计数过的文件名
        Set<String> counted = new HashSet<>();
        for (FileObject fileObject : fileObjects) {
            String objectName = fileObject.getObjectName();
            // 移除前缀，得到相对路径
            String relativePath = objectName.substring(path.length());
            // 如果相对路径为空，或者就是它自己，跳过
            if (relativePath.isEmpty()) {
                continue;
            }
            // 检查是否包含'/'来区分文件和文件夹
            int slashIndex = relativePath.indexOf('/');

            if (slashIndex == -1) {
                // 不包含'/'，是直接子文件
                FileNodeResp node = getFileNodeResp(fileObject, relativePath, objectName);
                fileNodes.add(node);
            } else {
                // 包含'/'，说明在子文件夹下
                // 我们只取第一个'/'之前的部分，作为文件夹名
                String folderName = relativePath.substring(0, slashIndex);
                if (counted.contains(folderName)) {
                    continue;
                }
                counted.add(folderName);
                // 获取该文件夹下的文件数量
                int count = minioUtils.countFilePrefix(bucket, path + folderName);
                folderNames.add(Folder.builder()
                    .id(fileObject.getId())
                    .uploadTime(fileObject.getCreateTime())
                    .name(folderName)
                    .fileCount(count)

                    .build());
            }

        }
        // 4. 将去重后的文件夹名称转换为FileNode对象
        for (Folder folder : folderNames) {
            FileNodeResp node = new FileNodeResp();
            node.setId(folder.getId());
            node.setType("folder");
            node.setName(folder.getName());
            node.setUploadTime(folder.getUploadTime());
            node.setFileCount(folder.getFileCount());
            // 文件夹的路径要以'/'结尾
            node.setPath(path + folder.getName() + FILE_SPLIT);
            fileNodes.add(node);
        }

        // 可以按类型和名称排序，让文件夹显示在前面
        fileNodes.sort(Comparator.comparing(FileNodeResp::getUploadTime).reversed());
        return fileNodes;
    }

    @NotNull
    private FileNodeResp getFileNodeResp(FileObject fileObject, String relativePath, String objectName) {
        FileNodeResp node = new FileNodeResp();
        node.setId(fileObject.getId());
        node.setType("file");
        node.setName(relativePath.substring(relativePath.indexOf("_") + 1));
        node.setPath(objectName);
        node.setCategory(fileObject.getCategory());
        node.setAbility(fileObject.getAbility());
        node.setContentOverview(fileObject.getContentOverview());
        node.setFileStatus(fileObject.getFileStatus());
        node.setSize(mapFileSize(fileObject.getFileSize()));
        node.setUploadTime(fileObject.getCreateTime());
        return node;
    }

    private BigDecimal mapFileSize(Long fileSize) {
        BigDecimal divide = BigDecimal.valueOf(fileSize).divide(BigDecimal.valueOf(1024 * 1024), 2, RoundingMode.HALF_UP);
        if (divide.compareTo(BigDecimal.ZERO) == 0) {
            BigDecimal div = BigDecimal.valueOf(fileSize).divide(BigDecimal.valueOf(1024 * 1024), 4, RoundingMode.HALF_UP);
            if (div.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.valueOf(fileSize).divide(BigDecimal.valueOf(1024 * 1024), 6, RoundingMode.HALF_UP);
            } else {
                return div;
            }
        }
        return divide;

    }
}
