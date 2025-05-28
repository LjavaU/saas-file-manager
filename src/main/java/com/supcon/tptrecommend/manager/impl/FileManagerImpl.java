package com.supcon.tptrecommend.manager.impl;

import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.supcon.framework.tenant.core.getter.TenantContext;
import com.supcon.system.base.entity.AutoIdEntity;
import com.supcon.systemcommon.entity.SupRequestBody;
import com.supcon.systemcommon.exception.ServerException;
import com.supcon.systemcommon.exception.SupException;
import com.supcon.systemmanagerapi.dto.LoginInfoUserDTO;
import com.supcon.tptrecommend.common.utils.LoginUserUtils;
import com.supcon.tptrecommend.common.utils.MinioUtils;
import com.supcon.tptrecommend.convert.fileobject.FileObjectConvert;
import com.supcon.tptrecommend.dto.fileobject.FileObjectCreateReq;
import com.supcon.tptrecommend.dto.fileobject.FileObjectResp;
import com.supcon.tptrecommend.entity.FileObject;
import com.supcon.tptrecommend.manager.FileManager;
import com.supcon.tptrecommend.service.IFileObjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileManagerImpl implements FileManager {

    @Value("${minio.bucket}")
    private String bucket;

    public static final String FILE_SPLIT = "/";

    private final MinioUtils minioUtils;

    private final IFileObjectService fileObjectService;

    /**
     * 上传文件
     *
     * @param file 文件
     * @return {@link Boolean }
     * @author luhao
     * @date 2025/05/22 14:56:00
     */
    @Override
    public Long upload(MultipartFile file) {
        // 2. 生成对象键 (Object Key)
        String originalFilename = file.getOriginalFilename() == null ? "unknown" : file.getOriginalFilename();
        // 3. 生成唯一文件名
        String uniqueFilename = UUID.fastUUID().toString().replace("-", "") + "_" + originalFilename;
        LoginInfoUserDTO user = LoginUserUtils.getLoginUserInfo();
        // 4.拼装文件路径
        String objectKey = getPath(user) + uniqueFilename;

        try {
            // 上传到minio
            minioUtils.uploadFile(bucket, objectKey, file.getInputStream(), file.getContentType());
        } catch (Exception e) {
            log.error("文件:{}上传失败: ", objectKey, e);
            throw new ServerException("文件上传失败");
        }
        // 保存文件元数据 到数据库
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
        Optional.ofNullable(LoginUserUtils.getLoginUserInfo().getId()).ifPresent(id->{
            body.getData().put("userId", String.valueOf( LoginUserUtils.getLoginUserInfo().getId()));
        });
        return fileObjectService.pageAutoQuery(body).convert(FileObjectConvert.INSTANCE::convert);
    }
}
