package com.supcon.tptrecommend.manager.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.supcon.framework.tenant.core.getter.TenantContext;
import com.supcon.system.base.entity.AutoIdEntity;
import com.supcon.systemcommon.entity.IDList;
import com.supcon.systemcommon.entity.SupRequestBody;
import com.supcon.systemcommon.entity.SupResult;
import com.supcon.systemcommon.exception.ServerException;
import com.supcon.systemcommon.exception.SupException;
import com.supcon.systemmanagerapi.dto.LoginInfoUserDTO;
import com.supcon.tptrecommend.common.utils.LoginUserUtils;
import com.supcon.tptrecommend.common.utils.MinioUtils;
import com.supcon.tptrecommend.common.utils.ProcessProgressSupport;
import com.supcon.tptrecommend.convert.fileobject.FileObjectConvert;
import com.supcon.tptrecommend.dto.fileobject.FileObjectCreateReq;
import com.supcon.tptrecommend.dto.fileobject.FileObjectResp;
import com.supcon.tptrecommend.dto.fileobject.SingleFileQueryReq;
import com.supcon.tptrecommend.entity.FileObject;
import com.supcon.tptrecommend.feign.DataHubFeign;
import com.supcon.tptrecommend.feign.LlmFeign;
import com.supcon.tptrecommend.feign.entity.*;
import com.supcon.tptrecommend.manager.FileManager;
import com.supcon.tptrecommend.manager.FileParseManager;
import com.supcon.tptrecommend.service.IFileObjectService;
import io.minio.StatObjectResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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


    private final LlmFeign llmFeign;


    private final DataHubFeign dataHubFeign;

    private final Executor EXECUTOR = new ThreadPoolExecutor(4, 8,
        1000L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(100),
        new ThreadPoolExecutor.AbortPolicy());

    private final FileParseManager fileParseManager;

    public  static final   Map<Long,FileParseResp> CACHE = new ConcurrentHashMap<>();


    /**
     * 上传文件
     *
     * @param file       文件
     * @param attributes 属性
     * @return {@link Boolean }
     * @author luhao
     * @date 2025/05/22 14:56:00
     */
    @Override
    public Long upload(MultipartFile file, String attributes) {
        // 2. 生成对象键 (Object Key)
        String originalFilename = file.getOriginalFilename() == null ? "unknown" : file.getOriginalFilename();
        // 3. 生成唯一文件名
        String uniqueFilename = UUID.fastUUID().toString().replace("-", "") + "_" + originalFilename;
        LoginInfoUserDTO user = LoginUserUtils.getLoginUserInfo();
        // 4.拼装文件路径
        String objectKey = getPath(user) + uniqueFilename;
        // 5.上传文件到MinIO
        uploadToMinio(file, objectKey);
        // 保存文件元数据 到数据库
        Long fileId = saveMetadataToDB(file, user, objectKey, originalFilename);
        if (StrUtil.isBlank(attributes)) {
            CompletableFuture.runAsync(() -> {
                handleFileAnalysis(file,fileId);
            }, EXECUTOR);

        }
        return fileId;
    }


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
     * 使用LLM进行文件解析
     *
     * @param fileId           文件 ID
     * @param markdown         Markdown
     * @param originalFilename 原始文件名
     * @param headMarkdown     头部 Markdown
     * @author luhao
     * @date 2025/06/05 18:31:00
     */
    private void parseWithLLM(Long fileId, String markdown, String originalFilename, String headMarkdown) {
        FileParseResp parse = llmFeign.parse(FileParseReq.builder()
            .markdownContent(markdown)
            .headMarkdownContent(headMarkdown)
            .build());
        if (parse != null) {
            CACHE.put(fileId, parse);
            String category = FileObject.Category.getValueByCode(parse.getCategory());
            updateFileParseSuccess(fileId, category, parse.getSummary());
            ProcessProgressSupport.notifyParseComplete(fileId);
            buildDataAndSave(parse.getData(), originalFilename);
        } else {
            log.error("{}文件，大模型分析失败", originalFilename);
            updateFileStatus(fileId, FileObject.FileStatus.PARSE_FAILED.getValue());
            ProcessProgressSupport.notifyParseComplete(fileId);
        }
    }

    public void buildDataAndSave(JSONArray dataArray, String originalFilename) {
        if (CollectionUtil.isNotEmpty(dataArray)) {
            JSONObject obj = dataArray.getJSONObject(0);
            if (obj.containsKey("位号名称") && obj.containsKey("位号描述")) {
                List<TagInfoCreateReq> tagInfoCreateReqs = dataArray.stream().map(o -> {
                    JSONObject dataObj = (JSONObject) o;
                    TagInfoCreateReq tagInfoCreateReq = new TagInfoCreateReq();
                    tagInfoCreateReq.setTagName(dataObj.getStr("位号名称"));
                    tagInfoCreateReq.setTagDesc(dataObj.getStr("位号描述") + "_来源:" + originalFilename);
                    tagInfoCreateReq.setTagType(4);
                    tagInfoCreateReq.setUnit(dataObj.getStr("位号单位"));
                    return tagInfoCreateReq;
                }).collect(Collectors.toList());
                SupResult<List<TagInfoResp>> result = dataHubFeign.batchAdd(SupRequestBody.data(tagInfoCreateReqs));
                if (result.getSuccess()) {
                    log.info("位号数据保存成功");
                } else {
                    log.error("位号数据保存失败");
                }

            } else if (obj.containsKey("TIME")) {
                List<TagValueDTO> tagValueDTOS = dataArray.stream().map(o -> {
                    JSONObject dataObj = (JSONObject) o;
                    String time = dataObj.getStr("TIME");
                    Set<String> tagNames = dataObj.keySet();
                    tagNames.remove("TIME");
                    return tagNames.stream().map(tagName -> {
                        TagValueDTO tagValueDTO = new TagValueDTO();
                        tagValueDTO.setTagName(tagName);
                        tagValueDTO.setTagValue(dataObj.get(tagName));
                        tagValueDTO.setTagTime(LocalDateTime.parse(time, DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")));
                        tagValueDTO.setAppTime(LocalDateTime.parse(time, DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")));
                        tagValueDTO.setQuality(192L);
                        return tagValueDTO;
                    }).collect(Collectors.toList());
                }).flatMap(Collection::stream).collect(Collectors.toList());
                SupResult<Boolean> booleanSupResult = dataHubFeign.importTagValue(SupRequestBody.data(tagValueDTOS));
                if (booleanSupResult.getSuccess()) {
                    log.info("位号历史数据保存成功");
                } else {
                    log.error("数据历史保存失败");
                }

            } else if (obj.containsKey("回路名称") && obj.containsKey("回路描述")) {
                List<TagInfoCreateReq> tagInfoCreateReqs = dataArray.stream().map(o -> {
                        JSONObject dataObj = (JSONObject) o;
                        List<TagInfoCreateReq> tags = new ArrayList<>();
                        TagInfoCreateReq tagInfoCreateReq = new TagInfoCreateReq();
                        tagInfoCreateReq.setTagName(dataObj.getStr("测量值位号"));
                        tagInfoCreateReq.setTagDesc(dataObj.getStr("回路描述") + "_来源:" + originalFilename);
                        tagInfoCreateReq.setTagType(4);
                        tags.add(tagInfoCreateReq);
                        TagInfoCreateReq tagInfoCreateReq2 = new TagInfoCreateReq();
                        tagInfoCreateReq2.setTagName(dataObj.getStr("设定值位号"));
                        tagInfoCreateReq2.setTagDesc(dataObj.getStr("回路描述") + "_来源:" + originalFilename);
                        tagInfoCreateReq2.setTagType(4);
                        tags.add(tagInfoCreateReq2);
                        TagInfoCreateReq tagInfoCreateReq3 = new TagInfoCreateReq();
                        tagInfoCreateReq3.setTagName(dataObj.getStr("阀位值位号"));
                        tagInfoCreateReq3.setTagDesc(dataObj.getStr("回路描述") + "_来源:" + originalFilename);
                        tagInfoCreateReq3.setTagType(4);
                        tags.add(tagInfoCreateReq3);
                        TagInfoCreateReq tagInfoCreateReq4 = new TagInfoCreateReq();
                        tagInfoCreateReq4.setTagName(dataObj.getStr("控制模式位号"));
                        tagInfoCreateReq4.setTagDesc(dataObj.getStr("回路描述") + "_来源:" + originalFilename);
                        tagInfoCreateReq4.setTagType(4);
                        tags.add(tagInfoCreateReq4);
                        TagInfoCreateReq tagInfoCreateReq5 = new TagInfoCreateReq();
                        tagInfoCreateReq5.setTagName(dataObj.getStr("比例位号"));
                        tagInfoCreateReq5.setTagDesc(dataObj.getStr("回路描述") + "_来源:" + originalFilename);
                        tagInfoCreateReq5.setTagType(4);
                        tags.add(tagInfoCreateReq5);
                        TagInfoCreateReq tagInfoCreateReq6 = new TagInfoCreateReq();
                        tagInfoCreateReq6.setTagName(dataObj.getStr("积分位号"));
                        tagInfoCreateReq6.setTagDesc(dataObj.getStr("回路描述") + "_来源:" + originalFilename);
                        tagInfoCreateReq6.setTagType(4);
                        tags.add(tagInfoCreateReq6);
                        TagInfoCreateReq tagInfoCreateReq7 = new TagInfoCreateReq();
                        tagInfoCreateReq7.setTagName(dataObj.getStr("微分位号"));
                        tagInfoCreateReq7.setTagDesc(dataObj.getStr("回路描述") + "_来源:" + originalFilename);
                        tagInfoCreateReq7.setTagType(4);
                        tags.add(tagInfoCreateReq7);
                        return tags;
                    }).flatMap(Collection::stream)
                    .collect(Collectors.toList());
                SupResult<List<TagInfoResp>> result = dataHubFeign.batchAdd(SupRequestBody.data(tagInfoCreateReqs));
                if (result.getSuccess()) {
                    log.info("回路数据保存成功");
                } else {
                    log.error("回路数据保存失败");
                }

            }
        }

    }

    private void updateFileParseSuccess(Long fileId, String category, String summary) {
        FileObject fileObject = new FileObject();
        fileObject.setId(fileId);
        fileObject.setCategory(category);
        fileObject.setContentOverview(summary);
        fileObject.setFileStatus(FileObject.FileStatus.PARSED.getValue());
        fileObjectService.updateById(fileObject);
    }

    private void updateFileStatus(Long fileId, Integer status) {
        FileObject fileObject = new FileObject();
        fileObject.setId(fileId);
        fileObject.setFileStatus(status);
        fileObjectService.updateById(fileObject);
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
        return fileObjectService.pageAutoQuery(body).convert(FileObjectConvert.INSTANCE::convert);
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
     * 调用大模型进行文件分析
     *
     * @param file 文件
     * @param fileId 文件 ID
     * @author luhao
     * @since 2025/06/09 18:16:20
     */
    public void handleFileAnalysis(MultipartFile file, Long fileId) {
        FileObject fileObject = fileObjectService.getById(fileId);
        String originalFilename = file.getOriginalFilename();
        if (FileObject.FileStatus.UNPARSED.getValue().equals(fileObject.getFileStatus())) {
            // 模拟的方式推送处理进度
            ProcessProgressSupport.notifyProcessProgress(fileId);
            String fullContentMarkdown;
            String headMarkdown;
            try {
                fullContentMarkdown = fileParseManager.parseFileToMarkdown(file, false);
                headMarkdown = fileParseManager.parseFileToMarkdown(file,true);
            } catch (Exception e) {
                log.error("文件解析失败：{}", originalFilename, e);
                updateFileStatus(fileId, FileObject.FileStatus.PARSE_FAILED.getValue());
                ProcessProgressSupport.notifyParseComplete(fileId);
                return;
            }
            if (StrUtil.isAllNotBlank(fullContentMarkdown, headMarkdown)) {
                parseWithLLM(fileId, fullContentMarkdown, originalFilename, headMarkdown);
            } else {
                updateFileStatus(fileId, FileObject.FileStatus.PARSE_FAILED.getValue());
                ProcessProgressSupport.notifyParseComplete(fileId);
            }


        }
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
}
