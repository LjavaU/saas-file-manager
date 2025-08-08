package com.supcon.tptrecommend.manager.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
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
import com.supcon.tptrecommend.common.enums.*;
import com.supcon.tptrecommend.common.utils.FileUtils;
import com.supcon.tptrecommend.common.utils.LoginUserUtils;
import com.supcon.tptrecommend.common.utils.MinioUtils;
import com.supcon.tptrecommend.common.utils.ProcessProgressSupport;
import com.supcon.tptrecommend.convert.fileobject.FileObjectConvert;
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
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileManagerImpl implements FileManager {

    @Value("${minio.bucket}")
    private String bucket;

    public static final String FILE_SPLIT = "/";

    private final MinioUtils minioUtils;

    private final IFileObjectService fileObjectService;

    private final KnowledgeFeign knowledgeFeign;

    private final IFileRecommendationService fileRecommendationService;

    /**
     * 用于处理IO密集型任务
     * 核心线程为：CPU核心是*3
     * 为了保证服务的可用性，如果队列已满，则丢弃解析任务
     */
    private final Executor EXECUTOR = new ThreadPoolExecutor(6, 12,
        1000L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(30),
        new ThreadPoolExecutor.AbortPolicy());

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
        //  生成对象键 (Object Key)
        String originalFilename = file.getOriginalFilename() == null ? "unknown" : file.getOriginalFilename();
        // 生成唯一文件名
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
        // 上传文件到MinIO
        uploadToMinio(file, objectKey);
        // 保存文件元数据 到数据库
        Long fileId = saveMetadataToDB(file, user, objectKey, originalFilename);

        doFileProcess(fileId, originalFilename);

        return FileObjectConvert.INSTANCE.convert(fileObjectService.getById(fileId));
    }

    private void doFileProcess(Long fileId, String originalFilename) {
        Optional<FileAnalysisHandle> handler = fileAnalysisHandleFactory.getHandler(FileUtils.getFileSuffix(originalFilename));
        if (handler.isPresent()) {
            FileAnalysisHandle fileAnalysisHandle = handler.get();
            CompletableFuture.runAsync(TtlRunnable.get(() -> {
                fileAnalysisHandle.handleFileAnalysis(fileId);
            }), EXECUTOR).exceptionally(throwable -> {
                log.error("文件：{},在处理过程中失败", originalFilename, throwable);
                markFileAsParseFailed(fileId);
                return null;
            });
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

    private void markFileAsParseFailed(Long fileId) {
        updateFileStatusParseFailed(fileId);
        ProcessProgressSupport.notifyParseComplete(fileId);
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
        try {
            minioUtils.uploadFile(bucket, objectKey, file.getInputStream(), file.getContentType());
        } catch (Exception e) {
            log.error("文件上传到minio失败: {}", objectKey, e);
            throw new ServerException("文件上传失败");
        }
    }

    /**
     * 将元数据保存到数据库
     *
     * @param file             文件
     * @param user             用户
     * @param objectKey        对象键
     * @param originalFilename 原始文件名
     * @return {@link Long }
     * @author luhao
     * @since 2025/06/19 10:18:15
     */
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
        return TenantContext.getCurrentTenant() + FILE_SPLIT + user.getUsername() + FILE_SPLIT;


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
            // 删除文件的知识库
            KnowledgeFileUploadResp resp = knowledgeFeign.deleteKnowledgeBase(String.valueOf(fileObject.getUserId()), fileObject.getBucketName(), fileObject.getObjectName(), fileObject.getTenantId());
            if (Objects.isNull(resp) || resp.getCode() != HttpStatus.HTTP_OK) {
                log.error("删除文件对应的知识库失败: {}", fileObject.getObjectName());
            }
        }
        return true;
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
        String path = getPath(user) + folderName;
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
            path = getPath(LoginUserUtils.getLoginUserInfo());
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
                FileNodeResp node = getFileNodeResp(fileObject, relativePath, objectName, recommendationMap);
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
    private FileNodeResp getFileNodeResp(FileObject fileObject, String relativePath, String objectName, Map<Long, List<String>> recommendationMap) {
        FileNodeResp node = new FileNodeResp();
        node.setId(fileObject.getId());
        node.setType(FileKind.FILE.getValue());
        node.setName(relativePath.substring(relativePath.indexOf("_") + 1));
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
        String path = getPath(LoginUserUtils.getLoginUserInfo());
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
                Long id = fileObject.getId();
                if (isFile) {
                    // 文件节点
                    currentNode.findOrCreateChild(part, id, FileKind.FILE.getValue(), objectName, fileObject.getFileSize(), fileObject.getKnowledgeParseState());
                } else {
                    // 文件夹节点
                    currentNode = currentNode.findOrCreateChild(part, id, FileKind.FOLDER.getValue(), objectName.substring(0, objectName.lastIndexOf("/") + 1), null, null);
                }
            }

        }
        return root;
    }

    @Override
    public boolean update(FileAttributesUpdatedReq req) {
        Long fileId = req.getFileId();
        String objectName = req.getObjectName();
        if (Objects.isNull(fileId) && StrUtil.isBlank(objectName)) {
            throw new ClientException("必须提供文件ID或文件全路径参数中的任意一个");
        }
        String ability = req.getAbility();
        String category = req.getCategory();
        if (StrUtil.isAllBlank(ability, category)) {
            throw new ClientException("文件类别或者属性不能为空");
        }
        FileObject fileObject = fileObjectService.getOne(Wrappers.<FileObject>lambdaQuery()
            .eq(StrUtil.isNotBlank(objectName), FileObject::getObjectName, objectName)
            .eq(Objects.nonNull(fileId), FileObject::getId, fileId));
        if (Objects.isNull(fileObject)) {
            throw new ClientException("文件不存在");
        }
        // 根据类别标识获取类别
        FileCategoryAbilityAssociation abilityAssociation = FileCategoryAbilityAssociation.getCategoryByIdentifier(category);
        if (Objects.nonNull(abilityAssociation)) {
            // 获取类别的二级分类
            SubCategoryEnum categoryType = abilityAssociation.getCategoryType();
            // 获取类别的三级分类
            TagHistoryCategory thirdCategoryType = abilityAssociation.getTagHistoryCategory();
            LambdaUpdateWrapper<FileObject> lambdaUpdate = Wrappers.lambdaUpdate();

            // 更新二级分类
            Optional.ofNullable(categoryType)
                .map(type -> mergeUpdatedProperties(String.valueOf(type.getCode()), fileObject.getSubCategory()))
                .ifPresent(subcategory -> lambdaUpdate.set(FileObject::getSubCategory, subcategory));
            //更新三级分类
            Optional.ofNullable(thirdCategoryType)
                .map(type -> mergeUpdatedProperties(String.valueOf(type.getCode()), fileObject.getThirdLevelCategory()))
                .ifPresent(thirdCategory -> lambdaUpdate.set(FileObject::getThirdLevelCategory, thirdCategory));
            fileObjectService.update(new FileObject(), lambdaUpdate
                .eq(Objects.nonNull(fileId), AutoIdEntity::getId, fileId)
                .eq(StrUtil.isNotBlank(objectName), FileObject::getObjectName, objectName));
        }
        String updateAbility = mergeUpdatedProperties(ability, fileObject.getAbility());
        if (Objects.nonNull(updateAbility)) {
            fileObjectService.update(new FileObject(), Wrappers.<FileObject>lambdaUpdate()
                .set(FileObject::getAbility, updateAbility)
                .eq(Objects.nonNull(fileId), AutoIdEntity::getId, fileId)
                .eq(StrUtil.isNotBlank(objectName), FileObject::getObjectName, objectName));

        }
        return true;
    }

    private String mergeUpdatedProperties(String updateProperty, String sourceProperty) {
        if (StrUtil.isBlank(updateProperty)) {
            return null;
        }
        Stream<String> existPropertyStream = StrUtil.isNotBlank(updateProperty) ? Arrays.stream(updateProperty.split(",")) : Stream.empty();
        Stream<String> newPropertyStream = StrUtil.isNotBlank(sourceProperty) ? Arrays.stream(sourceProperty.split(",")) : Stream.empty();
        return Stream.concat(existPropertyStream, newPropertyStream)
            .map(String::trim)
            .filter(StrUtil::isNotBlank)
            .distinct()
            .collect(Collectors.joining(","));
    }
}
