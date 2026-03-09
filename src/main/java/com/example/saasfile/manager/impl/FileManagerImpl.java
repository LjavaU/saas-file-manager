package com.example.saasfile.manager.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.saasfile.common.enums.*;
import com.example.saasfile.common.utils.*;
import com.example.saasfile.convert.fileobject.FileObjectConvert;
import com.example.saasfile.dto.fileUpload.ExcelUploadRequest;
import com.example.saasfile.dto.fileobject.*;
import com.example.saasfile.dto.fileshare.FileShareRequest;
import com.example.saasfile.dto.mq.FileParseTaskMessage;
import com.example.saasfile.entity.FileObject;
import com.example.saasfile.entity.FileRecommendation;
import com.example.saasfile.feign.KnowledgeFeign;
import com.example.saasfile.feign.entity.knowledge.KnowledgeFileUploadResp;
import com.example.saasfile.integration.mq.FileParseTaskProducer;
import com.example.saasfile.manager.FileManager;
import com.example.saasfile.manager.strategy.FileAnalysisHandle;
import com.example.saasfile.manager.strategy.FileAnalysisHandleFactory;
import com.example.saasfile.service.IFileObjectService;
import com.example.saasfile.service.IFileRecommendationService;
import com.example.saasfile.support.exception.ClientException;
import com.example.saasfile.support.exception.ServerException;
import com.example.saasfile.support.tenant.TenantContext;
import com.example.saasfile.support.user.LoginInfoUserDTO;
import com.example.saasfile.support.web.IDList;
import com.example.saasfile.support.web.SupRequestBody;
import io.jsonwebtoken.Claims;
import io.minio.Result;
import io.minio.StatObjectResponse;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Slf4j
public class FileManagerImpl implements FileManager {
    public static final String FILE_SPLIT = "/";
    private static final String SHARED_FOLDER_PLACEHOLDER = "_shared";
    private static final int MAX_BATCH_UPLOAD_COUNT = 20;

    @Value("${minio.bucket}")
    private String bucket;

    private final MinioUtils minioUtils;
    private final IFileObjectService fileObjectService;
    private final KnowledgeFeign knowledgeFeign;
    private final IFileRecommendationService fileRecommendationService;
    private final Executor fileUploadExecutor;
    private final FileAnalysisHandleFactory fileAnalysisHandleFactory;
    private final JwtService jwtService;
    private final FileParseTaskProducer fileParseTaskProducer;
    private final Executor delExecutor = new ThreadPoolExecutor(10, 20, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>(50), new ThreadPoolExecutor.DiscardPolicy());

    public FileManagerImpl(MinioUtils minioUtils, IFileObjectService fileObjectService, KnowledgeFeign knowledgeFeign,
                           IFileRecommendationService fileRecommendationService, FileAnalysisHandleFactory fileAnalysisHandleFactory,
                           JwtService jwtService, FileParseTaskProducer fileParseTaskProducer,
                           @Qualifier("fileUploadTaskExecutor") Executor fileUploadExecutor) {
        this.minioUtils = minioUtils;
        this.fileObjectService = fileObjectService;
        this.knowledgeFeign = knowledgeFeign;
        this.fileRecommendationService = fileRecommendationService;
        this.fileAnalysisHandleFactory = fileAnalysisHandleFactory;
        this.jwtService = jwtService;
        this.fileParseTaskProducer = fileParseTaskProducer;
        this.fileUploadExecutor = fileUploadExecutor;
    }

    @Override
    public FileObjectResp upload(MultipartFile file, String path) {
        LoginInfoUserDTO user = LoginUserUtils.getLoginUserInfo();
        FileObjectResp resp = doUpload(file, path, user);
        doFileProcess(resp.getId(), user.getId(), resp.getOriginalName(), null);
        return resp;
    }

    private FileObjectResp doUpload(MultipartFile file, String path, LoginInfoUserDTO user) {
        String originalFilename = Optional.ofNullable(file.getOriginalFilename()).orElse("unknown");
        String objectKey = generateUniqueObjectKey(path, originalFilename, user.getUsername());
        String contentType = StrUtil.blankToDefault(file.getContentType(), "application/octet-stream");
        long size = file.getSize();
        uploadToMinio(file, objectKey);
        Long fileId = saveMetadataToDB(contentType, size, user, objectKey, originalFilename);
        return buildFileObjectResp(fileId, user, objectKey, originalFilename, contentType, size);
    }

    private String generateUniqueObjectKey(String path, String originalFilename, String userName) {
        String uniqueFilename = UUID.fastUUID().toString().replace("-", "") + "_" + originalFilename;
        return resolveUploadPath(path, userName) + uniqueFilename;
    }

    private String resolveUploadPath(String path, String userName) {
        if (StrUtil.isBlank(path)) {
            return getPath(userName);
        }
        String normalizedPath = path.endsWith(FILE_SPLIT) ? path : path + FILE_SPLIT;
        String userPath = getPath(userName) + normalizedPath;
        long count = fileObjectService.count(Wrappers.<FileObject>lambdaQuery().eq(FileObject::getObjectName, userPath));
        return count > 0 ? userPath : getSharedPath() + normalizedPath;
    }

    private FileObjectResp buildFileObjectResp(Long fileId, LoginInfoUserDTO user, String objectName, String originalFilename, String contentType, long size) {
        FileObjectResp resp = new FileObjectResp();
        resp.setId(fileId);
        resp.setCreateTime(LocalDateTime.now());
        resp.setUpdateTime(LocalDateTime.now());
        resp.setTenantId(TenantContext.getCurrentTenant());
        resp.setUserId(user.getId());
        resp.setUserName(user.getUsername());
        resp.setObjectName(objectName);
        resp.setOriginalName(originalFilename);
        resp.setBucketName(bucket);
        resp.setContentType(contentType);
        resp.setFileSize(FileObjectConvert.INSTANCE.mapFileSize(size));
        resp.setFileStatus(FileStatus.UNPARSED.getValue());
        return resp;
    }

    private void doFileProcess(Long fileId, Long userId, String originalFilename, Integer category) {
        Optional<FileAnalysisHandle> handler = fileAnalysisHandleFactory.getHandler(FilenameUtils.getExtension(originalFilename));
        if (!handler.isPresent()) {
            updateFileParseStatus(fileId, FileStatus.PARSE_NOT_SUPPORT);
            ProcessProgressSupport.notifyParseComplete(fileId, userId);
            return;
        }
        FileParseTaskMessage message = FileParseTaskMessage.builder()
            .fileId(fileId).userId(userId).originalFilename(originalFilename).category(category)
            .tenantId(TenantContext.getCurrentTenant()).build();
        try {
            fileParseTaskProducer.send(message);
        } catch (Exception e) {
            log.error("Failed to publish parse task. fileId={}", fileId, e);
            updateFileParseStatus(fileId, FileStatus.PARSE_FAILED);
            ProcessProgressSupport.notifyParseComplete(fileId, userId);
        }
    }

    private void updateFileParseStatus(Long fileId, FileStatus status) {
        FileObject fileObject = new FileObject();
        fileObject.setId(fileId);
        fileObject.setFileStatus(status.getValue());
        fileObjectService.updateById(fileObject);
    }

    private void uploadToMinio(MultipartFile file, String objectKey) {
        try (InputStream inputStream = file.getInputStream()) {
            minioUtils.uploadFile(bucket, objectKey, inputStream, file.getContentType(), file.getSize());
        } catch (Exception e) {
            log.error("Failed to upload file. objectKey={}", objectKey, e);
            throw new ServerException("File upload failed");
        }
    }

    private Long saveMetadataToDB(String contentType, long size, LoginInfoUserDTO user, String objectKey, String originalFilename) {
        FileObjectCreateReq createReq = FileObjectCreateReq.builder()
            .userId(user.getId()).userName(user.getUsername()).objectName(objectKey).originalName(originalFilename)
            .bucketName(bucket).contentType(contentType).fileSize(size).build();
        String candidateName = originalFilename;
        for (int i = 0; i < 3; i++) {
            try {
                createReq.setOriginalName(candidateName);
                return fileObjectService.saveObj(createReq);
            } catch (DataIntegrityViolationException e) {
                String extension = FilenameUtils.getExtension(candidateName);
                String baseName = FilenameUtils.getBaseName(candidateName);
                candidateName = baseName + "_" + ShortIdGenerator.generate(6) + (extension.isEmpty() ? "" : "." + extension);
            }
        }
        throw new ServerException("Failed to save file metadata");
    }

    public String getPath(String userName) { return TenantContext.getCurrentTenant() + FILE_SPLIT + userName + FILE_SPLIT; }
    public String getSharedPath() { return TenantContext.getCurrentTenant() + FILE_SPLIT + SHARED_FOLDER_PLACEHOLDER + FILE_SPLIT; }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        FileObject fileObject = fileObjectService.getOne(Wrappers.<FileObject>query()
            .eq("id", id).eq("user_name", LoginUserUtils.getLoginUserInfo().getUsername()));
        if (fileObject == null) throw new ClientException("File not found");
        deleteFileObjectHierarchy(fileObject.getObjectName(), id);
        minioUtils.removeFile(fileObject.getBucketName(), fileObject.getObjectName());
        cleanupKnowledgeArtifacts(Collections.singletonList(fileObject));
        return true;
    }

    public void removeKnowledge(FileObject fileObject) {
        CompletableFuture.runAsync(() -> {
            KnowledgeFileUploadResp<?> resp = knowledgeFeign.deleteKnowledgeBase(
                String.valueOf(fileObject.getUserId()), fileObject.getBucketName(), fileObject.getObjectName(), fileObject.getTenantId());
            if (resp == null || resp.getCode() != HttpStatus.HTTP_OK) {
                log.error("Failed to delete knowledge data. object={}", fileObject.getObjectName());
            }
        }, delExecutor).exceptionally(throwable -> { log.error("Async knowledge cleanup failed", throwable); return null; });
    }

    private void cleanupKnowledgeArtifacts(List<FileObject> fileObjects) {
        for (FileObject fileObject : fileObjects) {
            if (!FileUtils.isKnowledgeDocumentFile(fileObject.getOriginalName())) continue;
            fileRecommendationService.remove(Wrappers.<FileRecommendation>lambdaQuery().eq(FileRecommendation::getFileId, fileObject.getId()));
            removeKnowledge(fileObject);
        }
    }

    private void deleteFileObjectHierarchy(String objectName, long id) {
        if (objectName.endsWith(FILE_SPLIT)) {
            fileObjectService.remove(Wrappers.<FileObject>lambdaQuery().likeRight(FileObject::getObjectName, objectName));
        } else {
            fileObjectService.removeById(id);
        }
    }

    @Override
    public IPage<FileObjectResp> selectPage(SupRequestBody<Map<String, String>> body) throws Exception {
        body.getData().put("userName", LoginUserUtils.getLoginUserInfo().getUsername());
        Optional.ofNullable(LoginUserUtils.getLoginUserInfo().getId()).ifPresent(id -> body.getData().put("userId", String.valueOf(id)));
        return fileObjectService.pageAutoQuery(new QueryWrapper<FileObject>().apply("object_name NOT LIKE {0}", "%/"), body)
            .convert(FileObjectConvert.INSTANCE::convert);
    }

    @Override
    public void getOne(SingleFileQueryReq req, HttpServletResponse response) {
        try {
            StatObjectResponse metadata = minioUtils.getMetadata(bucket, req.getPath());
            response.setContentType(metadata.contentType());
            response.setContentLengthLong(metadata.size());
            String name = FileUtils.getOriginalFileNameFromObjectKey(req.getPath());
            String encoded = URLEncoder.encode(name, StandardCharsets.UTF_8.name()).replace("+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=UTF-8''" + encoded);
            try (InputStream inputStream = minioUtils.getFileInputStream(bucket, req.getPath())) {
                IOUtils.copy(inputStream, response.getOutputStream());
            }
            response.flushBuffer();
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            log.error("File download failed", e);
        }
    }

    @Override
    public List<FileObject> detail(FileDetailReq req) {
        return fileObjectService.list(Wrappers.<FileObject>lambdaQuery().in(FileObject::getObjectName, req.getPaths()).isNotNull(FileObject::getKnowledgeParseState));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchDelete(IDList<Long> data) {
        List<Long> ids = Optional.ofNullable(data.getIds()).orElse(Collections.emptyList());
        if (ids.isEmpty()) return true;
        List<FileObject> fileObjects = fileObjectService.list(Wrappers.<FileObject>query()
            .in("id", ids).eq("user_name", LoginUserUtils.getLoginUserInfo().getUsername()));
        if (fileObjects.isEmpty()) return true;
        fileObjectService.removeBatchByIds(fileObjects.stream().map(file -> file.getId()).collect(Collectors.toList()));
        cleanupKnowledgeArtifacts(fileObjects);
        minioUtils.removeFiles(bucket, fileObjects.stream().map(FileObject::getObjectName).collect(Collectors.toList()));
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createFolder(CreateFolderReq data) {
        LoginInfoUserDTO user = LoginUserUtils.getLoginUserInfo();
        String folderName = Optional.ofNullable(data.getFolderName()).orElse("").trim().replaceAll("[\\n\\r]", "");
        if (StrUtil.isBlank(folderName)) throw new ClientException("Folder name cannot be blank");
        if (folderName.contains("_") || folderName.contains(FILE_SPLIT)) throw new ClientException("Folder name cannot contain '_' or '/'");
        String folderNameWithSlash = folderName + FILE_SPLIT;
        boolean shared = FolderTypeEnum.TENANT.getCode().equals(data.getFolderType());
        String basePath = shared ? getSharedPath() + folderNameWithSlash : getPath(user.getUsername()) + folderNameWithSlash;
        if ((shared && isFolderNameExistsInTenant(folderNameWithSlash)) || (!shared && isFolderNameExistsInUser(basePath, folderNameWithSlash))) {
            throw new ClientException("Folder already exists");
        }
        saveFolderToDB(user, basePath);
        minioUtils.createFolder(bucket, basePath);
        return true;
    }

    private boolean isFolderNameExistsInTenant(String folderNameWithSlash) {
        return fileObjectService.count(Wrappers.<FileObject>lambdaQuery().likeLeft(FileObject::getObjectName, FILE_SPLIT + folderNameWithSlash)) > 0;
    }

    private boolean isFolderNameExistsInUser(String path, String folderNameWithSlash) {
        return fileObjectService.count(Wrappers.<FileObject>lambdaQuery().eq(FileObject::getObjectName, path)) > 0
            || fileObjectService.count(Wrappers.<FileObject>lambdaQuery().eq(FileObject::getObjectName, getSharedPath() + folderNameWithSlash)) > 0;
    }

    private void saveFolderToDB(LoginInfoUserDTO user, String path) {
        fileObjectService.saveObj(FileObjectCreateReq.builder().userId(user.getId()).userName(user.getUsername()).objectName(path).bucketName(bucket).build());
    }

    @Override
    public List<FileNodeResp> listFiles(String path) {
        String userPath = getPath(LoginUserUtils.getLoginUserInfo().getUsername());
        String sharedPath = getSharedPath();
        LambdaQueryWrapper<FileObject> queryWrapper = Wrappers.lambdaQuery();
        if (StrUtil.isBlank(path)) {
            queryWrapper.likeRight(FileObject::getObjectName, userPath).or().likeRight(FileObject::getObjectName, sharedPath);
        } else {
            queryWrapper.likeRight(FileObject::getObjectName, path);
        }
        List<FileObject> fileObjects = fileObjectService.list(queryWrapper);
        Map<Long, List<String>> recommendationMap = loadFileRecommendations(fileObjects);
        List<FileNodeResp> nodes = new ArrayList<>();
        Set<String> folderPaths = new LinkedHashSet<>();
        for (FileObject fileObject : fileObjects) {
            String objectName = fileObject.getObjectName();
            String currentRootPath = StrUtil.isBlank(path) ? getPathForRoot(objectName, userPath, sharedPath) : path;
            if (!objectName.startsWith(currentRootPath)) continue;
            String relativePath = objectName.substring(currentRootPath.length());
            if (relativePath.isEmpty()) continue;
            int slashIndex = relativePath.lastIndexOf(FILE_SPLIT);
            if (slashIndex == -1) {
                nodes.add(getFileNodeResp(fileObject, objectName, recommendationMap));
                continue;
            }
            if (relativePath.indexOf(FILE_SPLIT) != relativePath.length() - 1) continue;
            String folderName = relativePath.substring(0, relativePath.indexOf(FILE_SPLIT));
            String folderPath = currentRootPath + folderName + FILE_SPLIT;
            if (!folderPaths.add(folderPath)) continue;
            getFileFolderNodeResp(currentRootPath, fileObject, folderName, minioUtils.countFilePrefix(bucket, currentRootPath + folderName), nodes);
        }
        nodes.sort(Comparator.comparing(FileNodeResp::getFolderType, Comparator.nullsLast(Comparator.reverseOrder()))
            .thenComparing(FileNodeResp::getType, Comparator.reverseOrder())
            .thenComparing(FileNodeResp::getUploadTime, Comparator.reverseOrder()));
        return nodes;
    }

    private String getPathForRoot(String objectName, String userPath, String sharePath) {
        if (objectName.startsWith(userPath)) return userPath;
        if (objectName.startsWith(sharePath)) return sharePath;
        return "";
    }

    private Map<Long, List<String>> loadFileRecommendations(List<FileObject> fileObjects) {
        List<Long> fileIds = fileObjects.stream().map(file -> file.getId()).filter(Objects::nonNull).collect(Collectors.toList());
        if (CollUtil.isEmpty(fileIds)) return Collections.emptyMap();
        return fileRecommendationService.list(Wrappers.<FileRecommendation>lambdaQuery().in(FileRecommendation::getFileId, fileIds)).stream()
            .filter(item -> StrUtil.isNotBlank(item.getQuestions()))
            .collect(Collectors.toMap(FileRecommendation::getFileId, item -> JSONUtil.toList(item.getQuestions(), String.class), (a, b) -> a));
    }

    private void getFileFolderNodeResp(String path, FileObject fileObject, String folderName, int fileCount, List<FileNodeResp> fileNodes) {
        FileNodeResp node = new FileNodeResp();
        node.setId(fileObject.getId());
        node.setType(FileKind.FOLDER.getValue());
        node.setName(folderName);
        node.setUploadTime(fileObject.getCreateTime());
        node.setFileCount(fileCount);
        node.setPath(path + folderName + FILE_SPLIT);
        node.setTenantId(fileObject.getTenantId());
        node.setUserId(fileObject.getUserId());
        node.setFolderType(isSharedFolder(node.getPath()) ? FolderTypeEnum.TENANT.getCode() : FolderTypeEnum.PERSONAL.getCode());
        fileNodes.add(node);
    }

    private boolean isSharedFolder(String objectName) { return objectName.startsWith(getSharedPath()); }

    @NotNull
    private FileNodeResp getFileNodeResp(FileObject fileObject, String objectName, Map<Long, List<String>> recommendationMap) {
        FileNodeResp node = new FileNodeResp();
        node.setId(fileObject.getId());
        node.setType(FileKind.FILE.getValue());
        node.setName(fileObject.getOriginalName());
        node.setPath(objectName);
        node.setCategory(FileObjectConvert.INSTANCE.mapCategory(fileObject));
        if (StrUtil.isNotBlank(fileObject.getAbility())) {
            node.setAbility(FileCategoryAbilityAssociation.getAbilityDescriptionByAbility(Arrays.asList(fileObject.getAbility().split(","))));
        }
        node.setContentOverview(fileObject.getContentOverview());
        node.setFileStatus(fileObject.getFileStatus());
        node.setSize(FileUtils.formatFileSize(Optional.ofNullable(fileObject.getFileSize()).orElse(0L)));
        node.setUploadTime(fileObject.getCreateTime());
        node.setQuestions(recommendationMap.get(fileObject.getId()));
        node.setTenantId(fileObject.getTenantId());
        node.setUserId(fileObject.getUserId());
        return node;
    }

    @Override
    public FileTreeNode listFilesAsTree() {
        FileTreeNode root = new FileTreeNode(0L, "Files", FileKind.FOLDER.getValue(), "", null, null);
        String path = getPath(LoginUserUtils.getLoginUserInfo().getUsername());
        String sharedPath = getSharedPath();
        List<FileObject> fileObjects = fileObjectService.list(Wrappers.<FileObject>lambdaQuery()
            .likeRight(FileObject::getObjectName, path).or().likeRight(FileObject::getObjectName, sharedPath));
        for (FileObject fileObject : fileObjects) {
            String rootPath = getPathForRoot(fileObject.getObjectName(), path, sharedPath);
            if (!fileObject.getObjectName().startsWith(rootPath)) continue;
            String relativePath = fileObject.getObjectName().substring(rootPath.length());
            if (StrUtil.isBlank(relativePath)) continue;
            FileTreeNode currentNode = root;
            String[] parts = relativePath.split(FILE_SPLIT);
            for (int i = 0; i < parts.length; i++) {
                if (StrUtil.isBlank(parts[i])) continue;
                boolean isFile = i == parts.length - 1 && StrUtil.isAllNotEmpty(fileObject.getOriginalName(), fileObject.getContentType());
                currentNode = currentNode.findOrCreateChild(parts[i], isFile ? FileKind.FILE.getValue() : FileKind.FOLDER.getValue(), fileObject);
            }
        }
        return root;
    }

    @Override
    public boolean update(FileAttributesUpdatedReq req) {
        validateRequest(req);
        FileObject fileObject = findFileOrFail(req.getObjectName());
        FileAttributesUpdatedCondition updated = new FileAttributesUpdatedCondition();
        updateCategoryProperties(req.getCategory(), fileObject, updated);
        updateAbilityProperty(req.getAbility(), fileObject, updated);
        if (StrUtil.isAllBlank(updated.getSubCategory(), updated.getThirdLevelCategory(), updated.getAbility())) return true;
        updated.setObjectName(req.getObjectName());
        fileObjectService.updateFileAttributes(updated);
        return true;
    }

    private void validateRequest(FileAttributesUpdatedReq req) {
        if (StrUtil.isAllBlank(req.getAbility(), req.getCategory())) throw new ClientException("Ability or category is required");
    }

    private FileObject findFileOrFail(String objectName) {
        FileObject fileObject = fileObjectService.getByObjectName(objectName);
        if (fileObject == null) throw new ClientException("File not found");
        return fileObject;
    }

    private void updateCategoryProperties(String categoryIdentifier, FileObject fileObject, FileAttributesUpdatedCondition updated) {
        if (StrUtil.isBlank(categoryIdentifier)) return;
        FileCategoryAbilityAssociation association = FileCategoryAbilityAssociation.getCategoryByIdentifier(categoryIdentifier);
        if (association == null) return;
        Optional.ofNullable(association.getCategoryType()).map(type -> mergeProperties(String.valueOf(type.getCode()), fileObject.getSubCategory())).ifPresent(updated::setSubCategory);
        Optional.ofNullable(association.getTagHistoryCategory()).map(type -> mergeProperties(String.valueOf(type.getCode()), fileObject.getThirdLevelCategory())).ifPresent(updated::setThirdLevelCategory);
    }

    private void updateAbilityProperty(String newAbility, FileObject fileObject, FileAttributesUpdatedCondition updated) {
        String mergedAbility = mergeProperties(newAbility, fileObject.getAbility());
        if (mergedAbility != null) updated.setAbility(mergedAbility);
    }

    private String mergeProperties(String newProperties, String existingProperties) {
        if (StrUtil.isBlank(newProperties)) return null;
        Set<String> combined = new LinkedHashSet<>();
        Stream.of(newProperties.split(",")).map(String::trim).filter(StrUtil::isNotBlank).forEach(combined::add);
        if (StrUtil.isNotBlank(existingProperties)) Stream.of(existingProperties.split(",")).map(String::trim).filter(StrUtil::isNotBlank).forEach(combined::add);
        return String.join(",", combined);
    }

    @Override
    public Integer getFileStatus(Long fileId) {
        return Optional.ofNullable(fileObjectService.getById(fileId)).map(FileObject::getFileStatus).orElse(null);
    }

    @Override
    public void reIndexParse(Long fileId) {
        FileObject fileObject = fileObjectService.getById(fileId);
        if (fileObject == null) throw new ClientException("File not found");
        validateFileForReIndex(fileObject);
        Integer unparsedStatus = FileStatus.UNPARSED.getValue();
        if (!unparsedStatus.equals(fileObject.getFileStatus())) {
            fileObjectService.update(new FileObject(), Wrappers.<FileObject>lambdaUpdate().set(FileObject::getFileStatus, unparsedStatus).eq(FileObject::getObjectName, fileObject.getObjectName()));
        }
        doFileProcess(fileId, LoginUserUtils.getLoginUserInfo().getId(), fileObject.getOriginalName(), SubCategoryEnum.METRICS_BUSINESS_REPORT_DATA.getCode());
        fileObjectService.update(new FileObject(), Wrappers.<FileObject>lambdaUpdate()
            .set(FileObject::getSubCategory, SubCategoryEnum.METRICS_BUSINESS_REPORT_DATA.getCode())
            .set(FileObject::getAbility, FileCategoryAbilityAssociation.getAbilityBySubCategory(SubCategoryEnum.METRICS_BUSINESS_REPORT_DATA))
            .eq(FileObject::getObjectName, fileObject.getObjectName()));
    }

    private void validateFileForReIndex(FileObject fileObject) {
        if (!isSupportedFileType(fileObject.getOriginalName())) throw new ClientException("Only xlsx, xls, and csv files support re-index");
        if (isRestrictedSubCategory(fileObject.getSubCategory())) throw new ClientException("Current file category does not support re-index");
    }

    private boolean isRestrictedSubCategory(String subCategory) {
        if (StrUtil.isBlank(subCategory)) return false;
        List<Integer> values = Stream.of(subCategory.split(",")).map(String::trim).filter(StrUtil::isNotBlank).map(this::safeParseInt).filter(Objects::nonNull).collect(Collectors.toList());
        return values.contains(SubCategoryEnum.METRICS_BUSINESS_REPORT_DATA.getCode()) || values.contains(SubCategoryEnum.TAG_HISTORICAL_DATA.getCode());
    }

    private Integer safeParseInt(String value) { try { return Integer.parseInt(value); } catch (Exception e) { return null; } }
    private boolean isSupportedFileType(String originalName) { return StrUtil.isNotBlank(originalName) && Arrays.asList(".xlsx", ".xls", ".csv").stream().anyMatch(originalName.toLowerCase()::endsWith); }

    @Override
    public void convertFileToUpload(ExcelUploadRequest request) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (ExcelWriter excelWriter = EasyExcel.write(outputStream).build()) {
            int sheetNo = 0;
            for (Map.Entry<String, List<List<String>>> entry : request.getContent().entrySet()) {
                WriteSheet writeSheet = EasyExcel.writerSheet(sheetNo++, entry.getKey()).build();
                excelWriter.write(entry.getValue(), writeSheet);
            }
        } catch (Exception e) {
            log.error("Failed to convert content to Excel", e);
            throw new ClientException("Failed to convert content to Excel");
        }
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray())) {
            LoginInfoUserDTO user = LoginUserUtils.getLoginUserInfo();
            String fileName = request.getFileName().endsWith(".xlsx") ? request.getFileName() : request.getFileName() + ".xlsx";
            String objectKey = generateUniqueObjectKey(null, fileName, user.getUsername());
            String contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            int size = inputStream.available();
            minioUtils.uploadFile(bucket, objectKey, inputStream, contentType, size);
            Long fileId = saveMetadataToDB(contentType, size, user, objectKey, fileName);
            doFileProcess(fileId, user.getId(), fileName, null);
        } catch (io.minio.errors.MinioException e) {
            throw new ServerException("File upload failed");
        } catch (Exception e) {
            throw new ServerException("Failed to convert and upload Excel file");
        }
    }

    @Override
    public FileStatisticsResp fileStatistics() { return fileObjectService.getFileStatistics(); }

    @Override
    public List<FileObjectResp> batchUpload(List<MultipartFile> multipartFiles, String path) {
        if (CollUtil.isEmpty(multipartFiles)) throw new ServerException("No files provided");
        if (multipartFiles.size() > MAX_BATCH_UPLOAD_COUNT) throw new ClientException("Batch upload supports up to 20 files");
        LoginInfoUserDTO loginUser = LoginUserUtils.getLoginUserInfo();
        String currentTenant = TenantContext.getCurrentTenant();
        List<CompletableFuture<FileObjectResp>> futures = multipartFiles.stream().map(file -> CompletableFuture.supplyAsync(() -> {
            TenantContext.setCurrentTenant(currentTenant);
            try { return doUpload(file, path, loginUser); } finally { TenantContext.clear(); }
        }, fileUploadExecutor)).collect(Collectors.toList());
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        List<FileObjectResp> results = futures.stream().map(CompletableFuture::join).collect(Collectors.toList());
        results.forEach(fileResp -> doFileProcess(fileResp.getId(), loginUser.getId(), fileResp.getOriginalName(), null));
        return results;
    }

    @Override
    public void downloadTenantFilesAsZip(String tenantId, String userName, HttpServletResponse response) {
        String tenantPrefix;
        if (StrUtil.isAllNotBlank(tenantId, userName)) tenantPrefix = tenantId + FILE_SPLIT + userName + FILE_SPLIT;
        else if (StrUtil.isBlank(userName) && StrUtil.isNotBlank(tenantId)) tenantPrefix = tenantId + FILE_SPLIT;
        else tenantPrefix = "";
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + (StrUtil.isBlank(tenantId) ? "all" : tenantId) + "_files.zip\"");
        try (ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream())) {
            byte[] buffer = new byte[8192];
            for (Result<Item> result : minioUtils.listObjects(bucket, tenantPrefix)) {
                Item item = result.get();
                String fullObjectPath = item.objectName();
                if (fullObjectPath.endsWith(FILE_SPLIT)) continue;
                String relativePath = fullObjectPath.substring(tenantPrefix.length());
                try (InputStream fileStream = minioUtils.getFileInputStream(bucket, fullObjectPath)) {
                    zipOut.putNextEntry(new ZipEntry(relativePath));
                    int bytesRead;
                    while ((bytesRead = fileStream.read(buffer)) > 0) zipOut.write(buffer, 0, bytesRead);
                    zipOut.closeEntry();
                } catch (Exception e) {
                    log.error("Failed to add object to zip. object={}", fullObjectPath, e);
                }
            }
            zipOut.finish();
        } catch (Exception e) {
            log.error("Failed to download tenant files as zip", e);
            if (!response.isCommitted()) response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private String removeRelativePathUUID(String relativePath) {
        int lastSlashIndex = relativePath.lastIndexOf(FILE_SPLIT);
        if (lastSlashIndex != -1) {
            String prefixPart = relativePath.substring(0, lastSlashIndex + 1);
            String fileName = relativePath.substring(lastSlashIndex + 1);
            return prefixPart + fileName.substring(fileName.indexOf("_") + 1);
        }
        int underscoreIndex = relativePath.indexOf("_");
        return underscoreIndex != -1 ? relativePath.substring(underscoreIndex + 1) : relativePath;
    }

    @Override
    public String createShareLink(FileShareRequest request) {
        String token = jwtService.generateDownloadToken(request.getBucketName(), request.getObjectName(), request.getExpirationSecond());
        return UriComponentsBuilder.fromPath("/open-api/file/link-download").queryParam("ticket", token).toUriString();
    }

    @Override
    public ResponseEntity<StreamingResponseBody> linkDownload(String token) {
        Optional<Claims> claimsOptional = jwtService.validateAndParseToken(token);
        if (!claimsOptional.isPresent()) throw new ClientException("Invalid or expired share link");
        Claims claims = claimsOptional.get();
        String bucketName = jwtService.getBucket(claims);
        String objectName = jwtService.getObject(claims);
        StatObjectResponse metadata = minioUtils.getMetadata(bucketName, objectName);
        StreamingResponseBody responseBody = outputStream -> {
            try (InputStream inputStream = minioUtils.getFileInputStream(bucketName, objectName)) {
                StreamUtils.copy(inputStream, outputStream);
            } catch (Exception e) {
                log.error("Streaming error for object: {}", objectName, e);
            }
        };
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodeFilename(FileUtils.getFileNameFromObjectKey(objectName)) + "\"")
            .header(HttpHeaders.CONTENT_TYPE, metadata.contentType())
            .contentLength(metadata.size())
            .body(responseBody);
    }

    private String encodeFilename(String filename) {
        try { return URLEncoder.encode(filename, StandardCharsets.UTF_8.toString()).replace("+", "%20"); }
        catch (UnsupportedEncodingException e) { return filename; }
    }

    @Override
    public void reParse(Long fileId) {
        FileObject fileObject = fileObjectService.getById(fileId);
        if (fileObject == null) throw new ClientException("File not found");
        FileObject update = new FileObject();
        update.setId(fileId);
        update.setFileStatus(FileStatus.UNPARSED.getValue());
        fileObjectService.updateById(update);
        doFileProcess(fileId, LoginUserUtils.getLoginUserInfo().getId(), fileObject.getOriginalName(), null);
    }
}
