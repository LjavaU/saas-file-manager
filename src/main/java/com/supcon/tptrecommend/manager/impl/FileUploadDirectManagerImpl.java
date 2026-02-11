package com.supcon.tptrecommend.manager.impl;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.supcon.framework.tenant.core.getter.TenantContext;
import com.supcon.system.base.entity.AutoIdEntity;
import com.supcon.systemcommon.exception.ClientException;
import com.supcon.systemcommon.exception.ServerException;
import com.supcon.systemmanagerapi.dto.LoginInfoUserDTO;
import com.supcon.tptrecommend.common.enums.FileStatus;
import com.supcon.tptrecommend.common.utils.*;
import com.supcon.tptrecommend.dto.fileUpload.*;
import com.supcon.tptrecommend.dto.fileobject.FileObjectCreateReq;
import com.supcon.tptrecommend.dto.mq.FileParseTaskMessage;
import com.supcon.tptrecommend.entity.FileObject;
import com.supcon.tptrecommend.integration.mq.FileParseTaskProducer;
import com.supcon.tptrecommend.manager.FileUploadDirectManager;
import com.supcon.tptrecommend.manager.strategy.FileAnalysisHandle;
import com.supcon.tptrecommend.manager.strategy.FileAnalysisHandleFactory;
import com.supcon.tptrecommend.service.IFileObjectService;
import io.minio.StatObjectResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@Slf4j
public class FileUploadDirectManagerImpl implements FileUploadDirectManager {

    private static final long SINGLE_UPLOAD_MAX_SIZE = 200L * 1024 * 1024;
    private static final int URL_EXPIRE_SECONDS = 10 * 60;
    private static final int SESSION_EXPIRE_SECONDS = 24 * 60 * 60;
    private static final int MAX_NAME_RETRY = 3;
    private static final String MODE_SINGLE = "single";
    private static final String FILE_SPLIT = "/";
    private static final String SHARED_FOLDER_PLACEHOLDER = "_shared";
    private static final Pattern MD5_PATTERN = Pattern.compile("^[a-fA-F0-9]{32}$");

    @Value("${minio.bucket}")
    private String bucket;

    private final IFileObjectService fileObjectService;
    private final MinioUtils minioUtils;
    private final FileAnalysisHandleFactory fileAnalysisHandleFactory;
    private final JwtService jwtService;
    private final FileParseTaskProducer fileParseTaskProducer;

    public FileUploadDirectManagerImpl(IFileObjectService fileObjectService,
                                       MinioUtils minioUtils,
                                       FileAnalysisHandleFactory fileAnalysisHandleFactory,
                                       JwtService jwtService,
                                       FileParseTaskProducer fileParseTaskProducer) {
        this.fileObjectService = fileObjectService;
        this.minioUtils = minioUtils;
        this.fileAnalysisHandleFactory = fileAnalysisHandleFactory;
        this.jwtService = jwtService;
        this.fileParseTaskProducer = fileParseTaskProducer;
    }

    @Override
    public PresignedUploadResp createPresignedUploadUrl(PresignedUploadInitReq req) {
        validateSingleUploadReq(req);
        LoginInfoUserDTO loginUser = LoginUserUtils.getLoginUserInfo();
        String fileMd5 = normalizeMd5(req.getFileMd5());
        FileObject fileObject = createUploadingFile(loginUser, req.getOriginalName(), req.getContentType(), req.getFileSize(), req.getPath());

        String uploadId = jwtService.generateUploadSessionToken(
            fileObject.getId(),
            fileObject.getBucketName(),
            fileObject.getObjectName(),
            req.getFileSize(),
            fileMd5,
            1,
            MODE_SINGLE,
            SESSION_EXPIRE_SECONDS
        );

        return PresignedUploadResp.builder()
            .fileId(fileObject.getId())
            .bucketName(fileObject.getBucketName())
            .objectName(fileObject.getObjectName())
            .uploadId(uploadId)
            .uploadUrl(minioUtils.getPresignedPutUrl(fileObject.getBucketName(), fileObject.getObjectName(), URL_EXPIRE_SECONDS))
            .expireAt(System.currentTimeMillis() + URL_EXPIRE_SECONDS * 1000L)
            .build();
    }

    @Override
    public UploadCompleteResp uploadCallback(UploadCallbackReq req) {
        LoginInfoUserDTO loginUser = LoginUserUtils.getLoginUserInfo();
        JwtService.UploadSessionClaims claims = parseUploadSession(req.getUploadId(), req.getFileId(), MODE_SINGLE);
        FileObject fileObject = findOwnedFileOrFail(req.getFileId(), loginUser.getId());
        validateSessionAndFile(claims, fileObject);

        StatObjectResponse metadata = minioUtils.getMetadata(fileObject.getBucketName(), fileObject.getObjectName());
        validateUploadedSize(metadata.size(), claims.getFileSize());
        verifyMd5(claims.getFileMd5(), metadata.etag());
        verifyMd5(normalizeMd5(req.getFileMd5()), metadata.etag());
        verifyEtag(req.getEtag(), metadata.etag());

        boolean parseTriggered = markAsUploadedAndTriggerParse(fileObject);
        Integer status = getCurrentStatus(fileObject.getId());
        return buildCompleteResp(fileObject.getId(), fileObject.getObjectName(), metadata.etag(), parseTriggered, status);
    }

    @Override
    public MultipartUploadInitResp initMultipartUpload(MultipartUploadInitReq req) {
        validateMultipartInitReq(req);
        LoginInfoUserDTO loginUser = LoginUserUtils.getLoginUserInfo();
        FileObject fileObject = createUploadingFile(loginUser, req.getOriginalName(), req.getContentType(), req.getFileSize(), req.getPath());

        String uploadId = minioUtils.createMultipartUpload(
            fileObject.getBucketName(),
            fileObject.getObjectName(),
            fileObject.getContentType()
        );

        return MultipartUploadInitResp.builder()
            .fileId(fileObject.getId())
            .bucketName(fileObject.getBucketName())
            .objectName(fileObject.getObjectName())
            .uploadId(uploadId)
            .totalParts(req.getTotalParts())
            .expireAt(System.currentTimeMillis() + SESSION_EXPIRE_SECONDS * 1000L)
            .build();
    }

    @Override
    public MultipartUploadSignResp signMultipartPart(MultipartUploadSignReq req) {
        LoginInfoUserDTO loginUser = LoginUserUtils.getLoginUserInfo();
        validatePartNumber(req.getPartNumber());

        FileObject fileObject = findOwnedFileOrFail(req.getFileId(), loginUser.getId());
        if (!FileStatus.UPLOADING.getValue().equals(fileObject.getFileStatus())) {
            throw new ClientException("upload session is not in uploading status");
        }

        return MultipartUploadSignResp.builder()
            .partNumber(req.getPartNumber())
            .partObjectName(fileObject.getObjectName())
            .uploadUrl(minioUtils.getPresignedUploadPartUrl(
                fileObject.getBucketName(),
                fileObject.getObjectName(),
                req.getUploadId(),
                req.getPartNumber(),
                URL_EXPIRE_SECONDS
            ))
            .expireAt(System.currentTimeMillis() + URL_EXPIRE_SECONDS * 1000L)
            .build();
    }

    @Override
    public UploadCompleteResp completeMultipartUpload(MultipartUploadCompleteReq req) {
        LoginInfoUserDTO loginUser = LoginUserUtils.getLoginUserInfo();
        FileObject fileObject = findOwnedFileOrFail(req.getFileId(), loginUser.getId());

        if (!FileStatus.UPLOADING.getValue().equals(fileObject.getFileStatus())) {
            StatObjectResponse metadata = minioUtils.getMetadata(fileObject.getBucketName(), fileObject.getObjectName());
            return buildCompleteResp(fileObject.getId(), fileObject.getObjectName(), metadata.etag(), false, fileObject.getFileStatus());
        }

        minioUtils.completeMultipartUpload(
            fileObject.getBucketName(),
            fileObject.getObjectName(),
            req.getUploadId(),
            fileObject.getFileSize()
        );

        StatObjectResponse finalMetadata = minioUtils.getMetadata(fileObject.getBucketName(), fileObject.getObjectName());
        validateUploadedSize(finalMetadata.size(), fileObject.getFileSize());

        boolean parseTriggered = markAsUploadedAndTriggerParse(fileObject);
        Integer status = getCurrentStatus(fileObject.getId());
        return buildCompleteResp(fileObject.getId(), fileObject.getObjectName(), finalMetadata.etag(), parseTriggered, status);
    }

    private void validateSingleUploadReq(PresignedUploadInitReq req) {
        if (req.getFileSize() > SINGLE_UPLOAD_MAX_SIZE) {
            throw new ClientException("file exceeds 200MB, please use multipart upload");
        }
        normalizeMd5(req.getFileMd5());
    }

    private void validateMultipartInitReq(MultipartUploadInitReq req) {
        if (req.getTotalParts() == null || req.getTotalParts() < 1) {
            throw new ClientException("闂佸憡甯掑Λ娑欐櫠閺嶎厼鏋佸ù鍏兼綑濞呫倝鐓崶褎鍤囬柕鍡楃箲瀵板嫯顦辩紒?");
        }
        normalizeMd5(req.getFileMd5());
    }

    private void validatePartNumber(Integer partNumber) {
        if (partNumber == null || partNumber < 1) {
            throw new ClientException("invalid part number");
        }
    }

    private FileObject createUploadingFile(LoginInfoUserDTO user,
                                           String originalName,
                                           String contentType,
                                           Long fileSize,
                                           String path) {
        String safeOriginalName = sanitizeOriginalName(originalName);
        String objectKey = generateUniqueObjectKey(path, safeOriginalName, user.getUsername());
        String safeContentType = normalizeContentType(contentType);
        Long fileId = saveMetadataToDB(safeContentType, fileSize, user, objectKey, safeOriginalName);

        boolean updated = fileObjectService.update(new FileObject(), Wrappers.<FileObject>lambdaUpdate()
            .set(FileObject::getFileStatus, FileStatus.UPLOADING.getValue())
            .eq(AutoIdEntity::getId, fileId)
            .eq(FileObject::getUserId, user.getId()));
        if (!updated) {
            throw new ServerException("failed to create upload task");
        }

        FileObject fileObject = fileObjectService.getById(fileId);
        if (fileObject == null) {
            throw new ServerException("failed to create upload task");
        }
        return fileObject;
    }

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
        for (int i = 0; i < MAX_NAME_RETRY; i++) {
            try {
                fileId = fileObjectService.saveObj(createReq);
                break;
            } catch (DataIntegrityViolationException e) {
                String extension = FilenameUtils.getExtension(originalFilename);
                String baseName = FilenameUtils.getBaseName(originalFilename);
                originalFilename = baseName + "_" + ShortIdGenerator.generate(6)
                    + (extension.isEmpty() ? "" : "." + extension);
                createReq.setOriginalName(originalFilename);
            }
        }
        if (fileId == null) {
            throw new ServerException("failed to create upload task, please retry later");
        }
        return fileId;
    }

    private String generateUniqueObjectKey(String path, String originalFilename, String userName) {
        String uniqueFilename = UUID.fastUUID().toString().replace("-", "") + "_" + originalFilename;
        return resolveUploadPath(path, userName) + uniqueFilename;
    }

    private String resolveUploadPath(String path, String userName) {
        String normalizedPath = normalizePath(path);
        if (StrUtil.isBlank(normalizedPath)) {
            return getPath(userName);
        }
        String suffix = normalizedPath + FILE_SPLIT;
        String userPath = getPath(userName) + suffix;
        long count = fileObjectService.count(Wrappers.<FileObject>lambdaQuery()
            .eq(FileObject::getObjectName, userPath));
        if (count > 0) {
            return userPath;
        }
        return getSharedPath() + suffix;
    }

    private String getPath(String userName) {
        return TenantContext.getCurrentTenant() + FILE_SPLIT + userName + FILE_SPLIT;
    }

    private String getSharedPath() {
        return TenantContext.getCurrentTenant() + FILE_SPLIT + SHARED_FOLDER_PLACEHOLDER + FILE_SPLIT;
    }

    private String normalizePath(String path) {
        if (StrUtil.isBlank(path)) {
            return "";
        }
        String normalized = path.trim().replace("\\", "/");
        normalized = normalized.replaceAll("/+", "/");
        normalized = StrUtil.removePrefix(normalized, "/");
        normalized = StrUtil.removeSuffix(normalized, "/");
        if (normalized.contains("..")) {
            throw new ClientException("upload path is invalid");
        }
        if (normalized.length() > 256) {
            throw new ClientException("upload path is too long");
        }
        return normalized;
    }

    private String sanitizeOriginalName(String originalName) {
        String safeName = FilenameUtils.getName(StrUtil.nullToEmpty(originalName).trim());
        safeName = safeName.replaceAll("[\\r\\n]", "");
        if (StrUtil.isBlank(safeName)) {
            throw new ClientException("file name cannot be empty");
        }
        if (safeName.length() > 255) {
            throw new ClientException("file name is too long");
        }
        return safeName;
    }

    private String normalizeContentType(String contentType) {
        String safeType = StrUtil.blankToDefault(contentType, "application/octet-stream").trim();
        if (safeType.length() > 128) {
            throw new ClientException("contentType length exceeds limit");
        }
        return safeType;
    }

    private String normalizeMd5(String md5) {
        if (StrUtil.isBlank(md5)) {
            return null;
        }
        String normalized = md5.replace("\"", "").trim().toLowerCase(Locale.ROOT);
        if (!MD5_PATTERN.matcher(normalized).matches()) {
            throw new ClientException("invalid MD5 format");
        }
        return normalized;
    }

    private JwtService.UploadSessionClaims parseUploadSession(String uploadId, Long fileId, String expectedMode) {
        JwtService.UploadSessionClaims claims = jwtService.validateAndParseUploadToken(uploadId)
            .orElseThrow(() -> new ClientException("uploadId is invalid or expired"));
        if (!Objects.equals(claims.getFileId(), fileId)) {
            throw new ClientException("uploadId does not match fileId");
        }
        if (!StrUtil.equals(expectedMode, claims.getMode())) {
            throw new ClientException("upload mode does not match");
        }
        return claims;
    }

    private void validateSessionAndFile(JwtService.UploadSessionClaims claims, FileObject fileObject) {
        if (!StrUtil.equals(claims.getBucketName(), fileObject.getBucketName())
            || !StrUtil.equals(claims.getObjectName(), fileObject.getObjectName())) {
            throw new ClientException("upload session does not match file metadata");
        }
    }

    private FileObject findOwnedFileOrFail(Long fileId, Long userId) {
        FileObject fileObject = fileObjectService.getOne(Wrappers.<FileObject>lambdaQuery()
            .eq(AutoIdEntity::getId, fileId)
            .eq(FileObject::getUserId, userId));
        if (fileObject == null) {
            throw new ClientException("file not found");
        }
        return fileObject;
    }

    private void validateUploadedSize(long actualSize, long expectedSize) {
        if (actualSize != expectedSize) {
            throw new ClientException("uploaded file size mismatch");
        }
    }

private void verifyMd5(String expectedMd5, String etag) {
        if (StrUtil.isBlank(expectedMd5)) {
            return;
        }
        String normalizedEtag = normalizeEtag(etag);
        if (!StrUtil.equals(expectedMd5, normalizedEtag)) {
            throw new ClientException("MD5 validation failed");
        }
    }

    private void verifyEtag(String expectedEtag, String actualEtag) {
        if (StrUtil.isBlank(expectedEtag)) {
            return;
        }
        if (!StrUtil.equals(normalizeEtag(expectedEtag), normalizeEtag(actualEtag))) {
            throw new ClientException("ETag validation failed");
        }
    }

    private String normalizeEtag(String etag) {
        return StrUtil.nullToEmpty(etag).replace("\"", "").trim().toLowerCase(Locale.ROOT);
    }

    private boolean markAsUploadedAndTriggerParse(FileObject fileObject) {
        boolean updated = fileObjectService.update(new FileObject(), Wrappers.<FileObject>lambdaUpdate()
            .set(FileObject::getFileStatus, FileStatus.UNPARSED.getValue())
            .eq(AutoIdEntity::getId, fileObject.getId())
            .eq(FileObject::getUserId, fileObject.getUserId())
            .eq(FileObject::getFileStatus, FileStatus.UPLOADING.getValue()));
        if (updated) {
            doFileProcess(fileObject.getId(), fileObject.getUserId(), fileObject.getOriginalName());
        }
        return updated;
    }

    private Integer getCurrentStatus(Long fileId) {
        return Optional.ofNullable(fileObjectService.getById(fileId))
            .map(FileObject::getFileStatus)
            .orElse(null);
    }

    private UploadCompleteResp buildCompleteResp(Long fileId, String objectName, String etag, boolean parseTriggered, Integer status) {
        return UploadCompleteResp.builder()
            .fileId(fileId)
            .objectName(objectName)
            .etag(etag)
            .parseTriggered(parseTriggered)
            .fileStatus(status)
            .build();
    }

    private void doFileProcess(Long fileId, Long userId, String originalFilename) {
        Optional<FileAnalysisHandle> handler = fileAnalysisHandleFactory.getHandler(FilenameUtils.getExtension(originalFilename));
        if (!handler.isPresent()) {
            updateFileParseStatus(fileId, FileStatus.PARSE_NOT_SUPPORT);
            ProcessProgressSupport.notifyParseComplete(fileId, userId);
            return;
        }

        try {
            fileParseTaskProducer.send(FileParseTaskMessage.builder()
                .fileId(fileId)
                .userId(userId)
                .originalFilename(originalFilename)
                .tenantId(TenantContext.getCurrentTenant())
                .build());
        } catch (Exception ex) {
            log.error("File parse task publish to MQ failed. fileId={}", fileId, ex);
            updateFileParseStatus(fileId, FileStatus.PARSE_FAILED);
            ProcessProgressSupport.notifyParseComplete(fileId, userId);
        }
    }

    private void updateFileParseStatus(Long fileId, FileStatus status) {
        FileObject update = new FileObject();
        update.setId(fileId);
        update.setFileStatus(status.getValue());
        fileObjectService.updateById(update);
    }
}
