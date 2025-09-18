package com.supcon.tptrecommend.manager.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.ttl.TtlRunnable;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.supcon.framework.tenant.core.getter.TenantContext;
import com.supcon.system.base.entity.AutoIdEntity;
import com.supcon.systemcommon.entity.IDList;
import com.supcon.systemcommon.entity.SupRequestBody;
import com.supcon.systemcommon.exception.ClientException;
import com.supcon.systemcommon.exception.ServerException;
import com.supcon.systemmanagerapi.dto.LoginInfoUserDTO;
import com.supcon.tptrecommend.common.enums.FileCategoryAbilityAssociation;
import com.supcon.tptrecommend.common.enums.FileKind;
import com.supcon.tptrecommend.common.enums.FileStatus;
import com.supcon.tptrecommend.common.enums.SubCategoryEnum;
import com.supcon.tptrecommend.common.utils.*;
import com.supcon.tptrecommend.convert.fileobject.FileObjectConvert;
import com.supcon.tptrecommend.dto.fileUpload.ExcelUploadRequest;
import com.supcon.tptrecommend.dto.fileobject.*;
import com.supcon.tptrecommend.entity.FileObject;
import com.supcon.tptrecommend.entity.FileRecommendation;
import com.supcon.tptrecommend.feign.KnowledgeFeign;
import com.supcon.tptrecommend.feign.entity.knowledge.KnowledgeFileUploadResp;
import com.supcon.tptrecommend.manager.FileManager;
import com.supcon.tptrecommend.manager.strategy.FileAnalysisHandle;
import com.supcon.tptrecommend.manager.strategy.FileAnalysisHandleFactory;
import com.supcon.tptrecommend.service.IFileObjectService;
import com.supcon.tptrecommend.service.IFileRecommendationService;
import io.minio.StatObjectResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class FileManagerImpl implements FileManager {

    @Value("${minio.bucket}")
    private String bucket;

    public static final String FILE_SPLIT = "/";

    private final MinioUtils minioUtils;

    private final IFileObjectService fileObjectService;

    private final KnowledgeFeign knowledgeFeign;

    private final IFileRecommendationService fileRecommendationService;

    private final Executor fileProcessingExecutor;

    private final Executor fileUploadExecutor;

    private final FileAnalysisHandleFactory fileAnalysisHandleFactory;

    public FileManagerImpl(MinioUtils minioUtils,
                           IFileObjectService fileObjectService,
                           KnowledgeFeign knowledgeFeign,
                           IFileRecommendationService fileRecommendationService,
                           FileAnalysisHandleFactory fileAnalysisHandleFactory,
                           @Qualifier("fileProcessingTaskExecutor") Executor fileProcessingExecutor,
                           @Qualifier("fileUploadTaskExecutor") Executor fileUploadExecutor) {
        this.minioUtils = minioUtils;
        this.fileObjectService = fileObjectService;
        this.knowledgeFeign = knowledgeFeign;
        this.fileRecommendationService = fileRecommendationService;
        this.fileAnalysisHandleFactory = fileAnalysisHandleFactory;
        this.fileProcessingExecutor = fileProcessingExecutor;
        this.fileUploadExecutor = fileUploadExecutor;
    }

    private final Executor delExecutor = new ThreadPoolExecutor(
        10,
        20,
        10,
        TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(50),
        new ThreadPoolExecutor.DiscardPolicy());

    /**
     * 上传文件
     *
     * @param file 文件
     * @return {@link Boolean }
     * @author luhao
     * @date 2025/05/22 14:56:00
     */
    @Override
    public FileObjectResp upload(MultipartFile file, String path) {
        String originalFilename = file.getOriginalFilename() == null ? "unknown" : file.getOriginalFilename();
        LoginInfoUserDTO user = LoginUserUtils.getLoginUserInfo();
        // 生成对象键 (Object Key)
        String objectKey = generateUniqueObjectKey(path, originalFilename, user.getUsername());
        String contentType = file.getContentType();
        long size = file.getSize();
        // 上传文件到MinIO
        uploadToMinio(file, objectKey);
        // 保存文件元数据 到数据库
        Long fileId = saveMetadataToDB(contentType, size, user, objectKey, originalFilename);
        Long userId = user.getId();
        // 异步处理文件
        doFileProcess(fileId, userId, originalFilename, null);
        return buildFileObjectResp(fileId, user, objectKey, originalFilename, contentType, size);
    }

    /**
     * 生成唯一对象键
     *
     * @param path             路径
     * @param originalFilename 原始文件名
     * @param userName         用户
     * @return {@link String }
     * @author luhao
     * @since 2025/08/22 10:33:39
     *
     */
    private String generateUniqueObjectKey(String path, String originalFilename, String userName) {
        // 生成唯一文件名
        String uniqueFilename = UUID.fastUUID().toString().replace("-", "") + "_" + originalFilename;

        // 文件全路径
        String objectKey;
        if (StrUtil.isNotBlank(path)) {
            if (!path.endsWith(FILE_SPLIT)) {
                path += FILE_SPLIT;
            }
            objectKey = getPath(userName) + path + uniqueFilename;
        } else {
            objectKey = getPath(userName) + uniqueFilename;
        }
        return objectKey;
    }


    private FileObjectResp buildFileObjectResp(Long fileId, LoginInfoUserDTO user, String objectName, String originalFilename, String contentType, long size) {
        FileObjectResp fileObjectResp = new FileObjectResp();
        fileObjectResp.setId(fileId);
        fileObjectResp.setCreateTime(LocalDateTime.now());
        fileObjectResp.setUpdateTime(LocalDateTime.now());
        fileObjectResp.setTenantId(TenantContext.getCurrentTenant());
        fileObjectResp.setUserId(user.getId());
        fileObjectResp.setUserName(user.getUsername());
        fileObjectResp.setObjectName(objectName);
        fileObjectResp.setOriginalName(originalFilename);
        fileObjectResp.setBucketName(bucket);
        fileObjectResp.setContentType(contentType);
        fileObjectResp.setFileSize(FileObjectConvert.INSTANCE.mapFileSize(size));
        fileObjectResp.setFileStatus(FileStatus.UNPARSED.getValue());
        return fileObjectResp;

    }

    private void doFileProcess(Long fileId, Long userId, String originalFilename, Integer category) {
        Optional<FileAnalysisHandle> handler = fileAnalysisHandleFactory.getHandler(FilenameUtils.getExtension(originalFilename));
        if (handler.isPresent()) {
            FileAnalysisHandle fileAnalysisHandle = handler.get();
            try {
                CompletableFuture.runAsync(TtlRunnable.get(() -> {
                    fileAnalysisHandle.handleFileAnalysis(fileId, category);
                }), fileProcessingExecutor).exceptionally(throwable -> {
                    log.error("文件：{},在处理过程中失败", originalFilename, throwable);
                    markFileAsParseFailed(fileId, userId);
                    return null;
                });
            } catch (RejectedExecutionException e) {
                log.error("文件解析已经达到负载，拒绝执行");
                markFileAsParseFailed(fileId, userId);
            }
        } else {
            log.error("文件：{},不支持解析", originalFilename);
        }
    }


    private void updateFileStatusParseFailed(Long fileId) {
        FileObject fileObject = new FileObject();
        fileObject.setId(fileId);
        fileObject.setFileStatus(FileStatus.PARSE_FAILED.getValue());
        fileObjectService.updateById(fileObject);
    }

    private void markFileAsParseFailed(Long fileId, Long userId) {
        updateFileStatusParseFailed(fileId);
        ProcessProgressSupport.notifyParseComplete(fileId, userId);
    }


    /**
     * 上传到 MiniO
     *
     * @param file      文件
     * @param objectKey 对象键
     * @author luhao
     * @since 2025/06/19 10:18:08
     */
    private void uploadToMinio(MultipartFile file, String objectKey) {
        try (InputStream inputStream = file.getInputStream()) {
            minioUtils.uploadFile(bucket, objectKey, inputStream, file.getContentType(), file.getSize());
        } catch (Exception e) {
            log.error("文件上传到minio失败: {}", objectKey, e);
            throw new ServerException("文件上传失败");
        }
    }

    /**
     * 将元数据保存到数据库
     *
     * @param contentType      内容类型
     * @param size             大小
     * @param user             用户
     * @param objectKey        对象键
     * @param originalFilename 原始文件名
     * @return {@link Long }
     * @author luhao
     * @since 2025/06/19 10:18:15
     *
     *
     */
    private Long saveMetadataToDB(String contentType, long size, LoginInfoUserDTO user, String objectKey, String originalFilename) {
        FileObjectCreateReq createReq = FileObjectCreateReq.builder()
            .userId(user.getId())
            .userName(user.getUsername())
            .objectName(objectKey)
            .originalName(originalFilename)
            .bucketName(bucket)
            .contentType(contentType)
            .fileSize(size)
            .build();
        Long fileId = null;
        for (int i = 0; i < 3; i++) { // 最多重试3次
            try {
                fileId = fileObjectService.saveObj(createReq);
                break;
            } catch (DataIntegrityViolationException e) {
                // 文件名已存在，尝试重命名
                String extension = FilenameUtils.getExtension(originalFilename);
                String baseName = FilenameUtils.getBaseName(originalFilename);
                originalFilename = baseName + "_" + ShortIdGenerator.generate(6)
                    + (extension.isEmpty() ? "" : "." + extension);
                createReq.setOriginalName(originalFilename);
            }
        }
        if (fileId == null) {
            throw new ServerException("上传文件失败，请稍后再试");
        }
        return fileId;
    }


    /**
     * 获取路径
     * 按照租户/用户名/文件的方式
     *
     * @param userName 用户
     * @return {@link String }
     * @author luhao
     * @date 2025/05/22 14:11:08
     */
    public String getPath(String userName) {
        return TenantContext.getCurrentTenant() + FILE_SPLIT + userName + FILE_SPLIT;


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
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        // 根据id和用户名查询文件
        FileObject fileObject = fileObjectService.getOne(Wrappers.<FileObject>lambdaQuery()
            .eq(AutoIdEntity::getId, id)
            .eq(FileObject::getUserName, LoginUserUtils.getLoginUserInfo().getUsername()));
        if (fileObject == null) {
            throw new ClientException("文件不存在");
        }
        deleteFileObjectHierarchy(fileObject.getObjectName(), id);
        minioUtils.removeFile(fileObject.getBucketName(), fileObject.getObjectName());
        if (FileUtils.isKnowledgeDocumentFile(fileObject.getOriginalName())) {
            // 删除文件推荐问题
            fileRecommendationService.remove(Wrappers.<FileRecommendation>lambdaQuery()
                .eq(FileRecommendation::getFileId, id));
            removeKnowledge(fileObject);
        }
        return true;
    }

    public void removeKnowledge(FileObject fileObject) {
        CompletableFuture.runAsync(() -> {
            // 删除文件的知识库
            KnowledgeFileUploadResp resp = knowledgeFeign.deleteKnowledgeBase(String.valueOf(fileObject.getUserId()), fileObject.getBucketName(), fileObject.getObjectName(), fileObject.getTenantId());
            if (Objects.isNull(resp) || resp.getCode() != HttpStatus.HTTP_OK) {
                log.error("删除文件对应的知识库失败: {}", fileObject.getObjectName());
            }
        }, delExecutor).exceptionally(throwable -> {
            log.error("调用知识库删除接口出错", throwable);
            return null;
        });

    }

    private void deleteFileObjectHierarchy(String objectName, long id) {
        // 以“/”结尾的是目录，则删除目录及目录下的所有文件
        if (objectName.endsWith("/")) {
            fileObjectService.remove(Wrappers.<FileObject>lambdaQuery()
                .likeRight(FileObject::getObjectName, objectName));
        } else {
            fileObjectService.removeById(id);
        }


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

        return fileObjectService.pageAutoQuery(new QueryWrapper<FileObject>()
                .apply("object_name NOT LIKE {0}", "%/"),
            body).convert(FileObjectConvert.INSTANCE::convert);
    }

    /**
     * 获取单个文件流
     *
     * @param req      请求体，包含文件路径等信息
     * @param response 响应对象，用于输出文件流
     * @author luhao
     * @date 2025/05/29 17:24:04
     */
    @Override
    public void getOne(SingleFileQueryReq req, HttpServletResponse response) {
        String path = req.getPath();

        try {
            // 获取文件元数据
            StatObjectResponse metadata = minioUtils.getMetadata(bucket, path);

            // 设置响应头
            response.setContentType(metadata.contentType());
            response.setContentLengthLong(metadata.size());

            String originFileName = path.substring(path.indexOf("_") + 1);
            String encodedFileName = URLEncoder.encode(originFileName, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=UTF-8''" + encodedFileName);

            //  获取来自MinIO的实时文件流，并直接写入响应
            try (InputStream inputStream = minioUtils.getFileInputStream(bucket, path)) {
                IOUtils.copy(inputStream, response.getOutputStream());
            }
            response.flushBuffer();

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            log.error("文件下载异常", e);
        }
    }


    @Override
    public List<FileObject> detail(FileDetailReq req) {
        return fileObjectService.list(Wrappers.<FileObject>lambdaQuery()
            .in(FileObject::getObjectName, req.getPaths())
            .isNotNull(FileObject::getKnowledgeParseState));

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
        fileObjectService.removeBatchByIds(ids);

        // TODO：删除文件对应的知识库 循环调用
        fileObjects.forEach(fileObject -> {
            if (FileUtils.isKnowledgeDocumentFile(fileObject.getOriginalName())) {
                // 删除文件推荐问题
                fileRecommendationService.remove(Wrappers.<FileRecommendation>lambdaQuery()
                    .in(FileRecommendation::getFileId, fileObject.getId()));
                knowledgeFeign.deleteKnowledgeBase(String.valueOf(fileObject.getUserId()), bucket, fileObject.getObjectName(), fileObject.getTenantId());
            }
        });
        minioUtils.removeFiles(bucket, objectNames);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean createFolder(CreateFolderReq data) {
        LoginInfoUserDTO user = LoginUserUtils.getLoginUserInfo();
        // 去除文件夹名的空格和换行符
        String folderName = data.getFolderName().trim().replaceAll("[\\n\\r]", "");
        if (StrUtil.isBlank(folderName)) {
            throw new ClientException("文件夹名称不能为空");
        }
        if (folderName.contains("_") || folderName.contains("/")) {
            throw new ClientException("文件夹名称不能包含特殊字符/_");
        }
        // 确保 folderName 以斜杠结尾
        folderName += FILE_SPLIT;
        String path = getPath(user.getUsername()) + folderName;
        saveFolderToDB(user, path);
        minioUtils.createFolder(bucket, path);
        return true;
    }

    private void saveFolderToDB(LoginInfoUserDTO user, String path) {
        // 判断文件是否存在
        long count = fileObjectService.count(Wrappers.<FileObject>lambdaQuery()
            .likeRight(FileObject::getObjectName, path)
            .eq(FileObject::getUserId, user.getId())
            .eq(FileObject::getUserName, user.getUsername()));
        // 如果存在，则不保存
        if (count > 0) {
            throw new ClientException("已存在相同的文件夹名称");
        }
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
            path = getPath(LoginUserUtils.getLoginUserInfo().getUsername());
        }
        List<FileObject> fileObjects = fileObjectService.list(Wrappers.<FileObject>lambdaQuery()
            .likeRight(FileObject::getObjectName, path));
        // 获取文件id列表
        Map<Long, List<String>> recommendationMap = loadFileRecommendations(fileObjects);
        // 用于最终返回的列表
        List<FileNodeResp> fileNodes = new ArrayList<>();
        for (FileObject fileObject : fileObjects) {
            String objectName = fileObject.getObjectName();
            // 移除前缀，得到相对路径
            String relativePath = objectName.substring(path.length());
            // 如果相对路径为空，或者就是它自己，跳过
            if (relativePath.isEmpty()) {
                continue;
            }
            // 检查是否包含'/'来区分文件和文件夹
            int slashIndex = relativePath.lastIndexOf('/');

            if (slashIndex == -1) {
                // 不包含'/'，是直接子文件
                FileNodeResp node = getFileNodeResp(fileObject, objectName, recommendationMap);
                fileNodes.add(node);
            } else {
                // 包含'/'，说明在子文件夹下
                // 把含有文件的路径跳过，只把文件夹添加到列表中
                if (relativePath.indexOf("/") != relativePath.length() - 1) {
                    continue;
                }
                // 只取第一个'/'之前的部分，作为文件夹名
                String folderName = relativePath.substring(0, relativePath.indexOf('/'));
                // 获取该文件夹下的文件数量
                int count = minioUtils.countFilePrefix(bucket, path + folderName);
                getFileFolderNodeResp(path, fileObject, folderName, count, fileNodes);
            }

        }
        // 先按照文件夹进行排序，再按照上传时间进行排序
        fileNodes.sort(Comparator.comparing(FileNodeResp::getType).reversed().thenComparing(FileNodeResp::getUploadTime, Comparator.reverseOrder()));

        return fileNodes;
    }

    private Map<Long, List<String>> loadFileRecommendations(List<FileObject> fileObjects) {
        List<Long> fileIds = fileObjects.stream().map(FileObject::getId).collect(Collectors.toList());
        if (CollUtil.isEmpty(fileIds)) {
            return Collections.emptyMap();
        }
        List<FileRecommendation> recommendations = fileRecommendationService.list(Wrappers.<FileRecommendation>lambdaQuery().in(FileRecommendation::getFileId, fileIds));
        return recommendations.stream()
            .filter(fileRecommendation -> StrUtil.isNotBlank(fileRecommendation.getQuestions()))
            .collect(Collectors.toMap(FileRecommendation::getFileId, fileRecommendation -> JSONUtil.toList(fileRecommendation.getQuestions(), String.class)));
    }

    private void getFileFolderNodeResp(String path, FileObject fileObject, String folderName, int fileCount, List<FileNodeResp> fileNodes) {
        FileNodeResp node = new FileNodeResp();
        node.setId(fileObject.getId());
        node.setType(FileKind.FOLDER.getValue());
        node.setName(folderName);
        node.setUploadTime(fileObject.getCreateTime());
        node.setFileCount(fileCount);
        // 文件夹的路径要以'/'结尾
        node.setPath(path + folderName + FILE_SPLIT);
        fileNodes.add(node);
    }

    @NotNull
    private FileNodeResp getFileNodeResp(FileObject fileObject, String objectName, Map<Long, List<String>> recommendationMap) {
        FileNodeResp node = new FileNodeResp();
        node.setId(fileObject.getId());
        node.setType(FileKind.FILE.getValue());
        node.setName(fileObject.getOriginalName());
        node.setPath(objectName);
        node.setCategory(FileObjectConvert.INSTANCE.mapCategory(fileObject));
        String ability = fileObject.getAbility();
        if (StrUtil.isNotBlank(ability)) {
            node.setAbility(FileCategoryAbilityAssociation.getAbilityDescriptionByAbility(Arrays.asList(ability.split(","))));
        }
        node.setContentOverview(fileObject.getContentOverview());
        node.setFileStatus(fileObject.getFileStatus());
        node.setSize(FileUtils.formatFileSize(fileObject.getFileSize()));
        node.setUploadTime(fileObject.getCreateTime());
        node.setQuestions(recommendationMap.get(fileObject.getId()));
        return node;
    }

    /**
     * 将文件列转换为树
     *
     * @return {@link FileTreeNode }
     * @author luhao
     * @since 2025/07/03 16:22:05
     */
    @Override
    public FileTreeNode listFilesAsTree() {
        // 2. 创建根节点
        FileTreeNode root = new FileTreeNode(0L, "文件库", FileKind.FOLDER.getValue(), "", null, null);

        // 3. 递归列出所有对象
        String path = getPath(LoginUserUtils.getLoginUserInfo().getUsername());
        List<FileObject> fileObjects = fileObjectService.list(Wrappers.<FileObject>lambdaQuery()
            .likeRight(FileObject::getObjectName, path));
        // 4. 遍历对象并构建树
        for (FileObject fileObject : fileObjects) {
            String objectName = fileObject.getObjectName();
            // 移除前缀，得到相对路径
            String relativePath = objectName.substring(path.length());
            FileTreeNode currentNode = root;
            String[] parts = relativePath.split("/");
            for (int i = 0; i < parts.length; i++) {
                String part = parts[i];
                boolean isFile = (i == parts.length - 1 && StrUtil.isAllNotEmpty(fileObject.getOriginalName(), fileObject.getContentType()));
                if (isFile) {
                    // 文件节点
                    currentNode.findOrCreateChild(part, FileKind.FILE.getValue(), fileObject);
                } else {
                    // 文件夹节点
                    currentNode = currentNode.findOrCreateChild(part, FileKind.FOLDER.getValue(), fileObject);
                }
            }

        }
        return root;
    }

    /**
     * 更新文件类别、能力属性
     *
     * @param req 请求体
     * @return boolean
     * @author luhao
     * @since 2025/08/22 13:33:48
     *
     */
    @Override
    public boolean update(FileAttributesUpdatedReq req) {
        // 校验请求
        validateRequest(req);
        // 查找文件是否存在
        FileObject fileObject = findFileOrFail(req.getFileId(), req.getObjectName());

        // 为所有更改准备一个更新包装器。
        LambdaUpdateWrapper<FileObject> updateWrapper = new LambdaUpdateWrapper<>();

        // 处理类别更新（子类别和三级类别）。
        updateCategoryProperties(req.getCategory(), fileObject, updateWrapper);

        // 处理能力更新。
        updateAbilityProperty(req.getAbility(), fileObject, updateWrapper);

        // 仅当有更改时才执行单个数据库更新。
        if (StrUtil.isBlank(updateWrapper.getSqlSet())) {
            return true;
        }
        // 为更新添加 WHERE 子句
        updateWrapper.eq(Objects.nonNull(req.getFileId()), FileObject::getId, req.getFileId())
            .eq(StrUtil.isNotBlank(req.getObjectName()), FileObject::getObjectName, req.getObjectName());

        fileObjectService.update(new FileObject(), updateWrapper);
        return true;


    }

    private void validateRequest(FileAttributesUpdatedReq req) {
        if (Objects.isNull(req.getFileId()) && StrUtil.isBlank(req.getObjectName())) {
            throw new ClientException("必须提供文件ID或文件全路径参数中的任意一个");
        }
        if (StrUtil.isAllBlank(req.getAbility(), req.getCategory())) {
            throw new ClientException("文件类别或者属性不能为空");
        }
    }

    private FileObject findFileOrFail(Long fileId, String objectName) {
        FileObject fileObject = fileObjectService.limitOne(Wrappers.<FileObject>lambdaQuery()
            .eq(StrUtil.isNotBlank(objectName), FileObject::getObjectName, objectName)
            .eq(Objects.nonNull(fileId), FileObject::getId, fileId));
        if (Objects.isNull(fileObject)) {
            throw new ClientException("文件不存在");
        }
        return fileObject;
    }

    /**
     * 将类别相关字段添加到更新包装器。
     */
    private void updateCategoryProperties(String categoryIdentifier, FileObject fileObject, LambdaUpdateWrapper<FileObject> wrapper) {
        if (StrUtil.isBlank(categoryIdentifier)) {
            return;
        }

        FileCategoryAbilityAssociation association = FileCategoryAbilityAssociation.getCategoryByIdentifier(categoryIdentifier);
        if (association != null) {
            // 更新 sub-category (二级分类)
            Optional.ofNullable(association.getCategoryType())
                .map(type -> mergeProperties(String.valueOf(type.getCode()), fileObject.getSubCategory()))
                .ifPresent(merged -> wrapper.set(FileObject::getSubCategory, merged));

            // 更新 third-level category (三级分类)
            Optional.ofNullable(association.getTagHistoryCategory())
                .map(type -> mergeProperties(String.valueOf(type.getCode()), fileObject.getThirdLevelCategory()))
                .ifPresent(merged -> wrapper.set(FileObject::getThirdLevelCategory, merged));
        }
    }

    /**
     * 将能力字段添加到更新包装器。
     */
    private void updateAbilityProperty(String newAbility, FileObject fileObject, LambdaUpdateWrapper<FileObject> wrapper) {
        String mergedAbility = mergeProperties(newAbility, fileObject.getAbility());
        if (mergedAbility != null) {
            wrapper.set(FileObject::getAbility, mergedAbility);
        }
    }

    /**
     * 合并新的和现有的逗号分隔属性，确保唯一性。
     *
     * @param newProperties      要添加的新类别
     * @param existingProperties 现有的已存在类别.
     * @return {@link String } 合并后的类别
     * @author luhao
     * @since 2025/08/22 13:28:36
     *
     */
    private String mergeProperties(String newProperties, String existingProperties) {
        // 如果没有新属性，则表示不应对此字段进行更新。
        if (StrUtil.isBlank(newProperties)) {
            return null;
        }

        // 使用 LinkedHashSet 来维护插入顺序并保证唯一性。
        Set<String> combined = new LinkedHashSet<>();

        // 首先添加新属性.
        Stream.of(newProperties.split(","))
            .map(String::trim)
            .filter(StrUtil::isNotBlank)
            .forEach(combined::add);

        // 然后，添加现有属性。
        if (StrUtil.isNotBlank(existingProperties)) {
            Stream.of(existingProperties.split(","))
                .map(String::trim)
                .filter(StrUtil::isNotBlank)
                .forEach(combined::add);
        }

        return String.join(",", combined);
    }


    @Override
    public Integer getFileStatus(Long fileId) {
        return Optional.ofNullable(fileObjectService.getById(fileId))
            .map(FileObject::getFileStatus)
            .orElse(null);
    }

    /**
     * 重新进行指标文件的解析
     *
     * @param fileId 文件 ID
     * @author luhao
     * @since 2025/08/22 13:18:52
     *
     *
     */
    @Override
    public void reIndexParse(Long fileId) {
        FileObject fileObject = fileObjectService.getById(fileId);
        if (Objects.isNull(fileObject)) {
            throw new ClientException("文件不存在");
        }
        // 校验是否支持此操作
        validateFileForReIndex(fileObject);
        Integer unparsedStatus = FileStatus.UNPARSED.getValue();
        // 如果文件状态不是未解析状态，则将文件状态设置为未解析状态
        if (!unparsedStatus.equals(fileObject.getFileStatus())) {
            fileObjectService.update(new FileObject(), Wrappers.<FileObject>lambdaUpdate()
                .set(FileObject::getFileStatus, unparsedStatus)
                .eq(AutoIdEntity::getId, fileId));
        }
        Long userId = LoginUserUtils.getLoginUserInfo().getId();
        String originalName = fileObject.getOriginalName();
        Integer targetCategoryCode = SubCategoryEnum.METRICS_BUSINESS_REPORT_DATA.getCode();

        // 根据目标类型处理文件
        doFileProcess(fileId, userId, originalName, targetCategoryCode);

        // 更新元数据
        updateFileMetadataAfterReIndex(fileId, targetCategoryCode);


    }

    private void updateFileMetadataAfterReIndex(Long fileId, Integer targetCategoryCode) {
        fileObjectService.update(new FileObject(), Wrappers.<FileObject>lambdaUpdate()
            .set(FileObject::getSubCategory, targetCategoryCode)
            .set(FileObject::getAbility, FileCategoryAbilityAssociation.getAbilityBySubCategory(SubCategoryEnum.METRICS_BUSINESS_REPORT_DATA))
            .eq(AutoIdEntity::getId, fileId));
    }

    private void validateFileForReIndex(FileObject fileObject) {
        if (!isSupportedFileType(fileObject.getOriginalName())) {
            throw new ClientException("文件类型不支持。");
        }
        if (isRestrictedSubCategory(fileObject.getSubCategory())) {
            throw new ClientException("此文件是指标业务报表或位号历史数据，无需重复操作。");
        }
    }

    private boolean isRestrictedSubCategory(String subCategory) {
        if (StrUtil.isBlank(subCategory)) {
            return false;
        }
        Integer metricsBusinessReportCode = SubCategoryEnum.METRICS_BUSINESS_REPORT_DATA.getCode();
        Integer tagHistoricalDataCode = SubCategoryEnum.TAG_HISTORICAL_DATA.getCode();
        try {
            Integer subCategoryCode = Integer.parseInt(subCategory);
            return metricsBusinessReportCode.equals(subCategoryCode) || tagHistoricalDataCode.equals(subCategoryCode);
        } catch (NumberFormatException e) {
            return false;
        }
    }


    private boolean isSupportedFileType(String originalName) {
        if (StrUtil.isBlank(originalName)) {
            return false;
        }
        List<String> supportedExtensions = Arrays.asList(".xlsx", ".xls", ".csv");
        return supportedExtensions.stream().anyMatch(originalName::endsWith);
    }


    /**
     * 把结构内容转换为文件进行上传
     *
     * @param request 请求
     * @author luhao
     * @since 2025/08/22 13:17:45
     *
     *
     */
    @Override
    public void convertFileToUpload(ExcelUploadRequest request) {
        // 1. 使用ByteArrayOutputStream在内存中生成Excel
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // 创建ExcelWriter对象
        try (ExcelWriter excelWriter = EasyExcel.write(outputStream).build()) {
            int sheetNo = 0;
            // 遍历Map来创建多个Sheet
            for (Map.Entry<String, List<List<String>>> entry : request.getContent().entrySet()) {
                String sheetName = entry.getKey();
                List<List<String>> data = entry.getValue();
                // 创建一个Sheet
                WriteSheet writeSheet = EasyExcel.writerSheet(sheetNo++, sheetName).build();
                // 将数据写入Sheet
                excelWriter.write(data, writeSheet);
            }
        } catch (Exception e) {
            log.error("JSON转excel失败", e);
            throw new ClientException("转换excel过程失败");
        }
        // 2. 上传到MinIO
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray())) {
            LoginInfoUserDTO loginUserInfo = LoginUserUtils.getLoginUserInfo();
            String completeFileName = completeFileName(request.getFileName());
            // 生成唯一对象名称
            String objectKey = generateUniqueObjectKey(null, completeFileName, loginUserInfo.getUsername());
            String contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            int size = inputStream.available();
            // 上传文件
            minioUtils.uploadFile(bucket, objectKey, inputStream, contentType, size);
            // 保存元数据
            Long fileId = saveMetadataToDB(contentType, size, loginUserInfo, objectKey, completeFileName);
            // 创建文件处理任务
            doFileProcess(fileId, loginUserInfo.getId(), completeFileName, null);
        } catch (io.minio.errors.MinioException e) {
            throw new ServerException("文件上传失败");
        } catch (Exception e) {
            throw new ServerException("上传文件过程中发生未知异常");
        }


    }

    private String completeFileName(String fileName) {
        return fileName.endsWith(".xlsx") ? fileName : fileName + ".xlsx";
    }

    /**
     * 文件统计
     *
     * @return {@link FileStatisticsResp }
     * @author luhao
     * @since 2025/09/10 14:46:25
     *
     */
    @Override
    public FileStatisticsResp fileStatistics() {
        // 统计文件总数量和总大小
        return fileObjectService.getFileStatistics();
    }

    /**
     * 批量上传
     *
     * @param multipartFiles 多部分文件
     * @param path           路径
     * @return {@link List }<{@link FileObjectResp }>
     * @author luhao
     * @since 2025/09/17 19:52:40
     *
     */
    @Override
    public List<FileObjectResp> batchUpload(List<MultipartFile> multipartFiles, String path) {
        if (CollUtil.isEmpty(multipartFiles)) {
            throw new ServerException("上传文件不能为空");
        }
        // 限制文件为20个
        if (multipartFiles.size() > 20) {
            throw new ClientException("一次最多上传20个文件");
        }
        List<CompletableFuture<FileObjectResp>> futures = multipartFiles.stream()
            .map(file -> CompletableFuture.supplyAsync(() -> upload(file, path), fileUploadExecutor))
            .collect(Collectors.toList());

        // 使用 allOf 等待所有异步任务完成
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        // 等待所有任务完成并收集结果
        return allOf.thenApply(v -> futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList()))
            .join();
    }
}
