package com.supcon.tptrecommend.manager.strategy.impl;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.google.common.collect.Sets;
import com.supcon.systemcommon.exception.ServerException;
import com.supcon.tptrecommend.common.enums.*;
import com.supcon.tptrecommend.common.utils.*;
import com.supcon.tptrecommend.convert.filedata.DynamicMapper;
import com.supcon.tptrecommend.entity.FileObject;
import com.supcon.tptrecommend.feign.LlmFeign;
import com.supcon.tptrecommend.feign.entity.llm.FileAlignmentReq;
import com.supcon.tptrecommend.feign.entity.llm.FileAlignmentResp;
import com.supcon.tptrecommend.feign.entity.llm.FileClassifyReq;
import com.supcon.tptrecommend.feign.entity.llm.FileClassifyResp;
import com.supcon.tptrecommend.integration.excel.ExcelDataListener;
import com.supcon.tptrecommend.integration.excel.ExtraAttributesListener;
import com.supcon.tptrecommend.manager.strategy.BusinessDataHandler;
import com.supcon.tptrecommend.manager.strategy.BusinessDataHandlerFactory;
import com.supcon.tptrecommend.manager.strategy.FileAnalysisHandle;
import com.supcon.tptrecommend.manager.strategy.MapperFactory;
import com.supcon.tptrecommend.service.IFileObjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExcelFileAnalysisHandle implements FileAnalysisHandle {
    private final IFileObjectService fileObjectService;
    private final LlmFeign llmFeign;
    private final BusinessDataHandlerFactory businessDataHandlerFactory;
    private final MapperFactory mapperFactory;

    private final MinioUtils minioUtils;

    /**
     * 处理文件分析
     *
     * @param fileId 文件 ID
     * @author luhao
     * @since 2025/06/18 20:06:18
     */
    @Override
    public void handleFileAnalysis(Long fileId) {
        FileObject fileObject = fileObjectService.getById(fileId);
        if (Objects.isNull(fileObject)) {
            log.error("文件不存在，解析任务终止");
            return;
        }

        if (FileStatus.UNPARSED.getValue().equals(fileObject.getFileStatus())) {
            String originalFilename = fileObject.getOriginalName();
            String objectName = fileObject.getObjectName();
            String uniqueFilename = FileUtils.getFileNameFromObjectName(objectName);
            File file = null;
            try {
                file = minioUtils.saveStreamToTempFile(fileObject.getBucketName(), objectName, uniqueFilename);
                if (file == null) {
                    throw new ServerException("临时文件" + originalFilename + "保存失败");
                }
                doHandle(file, fileId, originalFilename,fileObject.getUserId());
            } finally {
                FileUtils.deleteTemporaryFile(file, originalFilename);
            }

        }
    }


    private void updateFileParseMetadata(Long fileId, String category, String summary, Integer subcategory, Integer thirdLevelCategory) {
        FileObject fileObject = new FileObject();
        fileObject.setId(fileId);
        fileObject.setCategory(category);
        fileObject.setContentOverview(summary);
        fileObject.setSubCategory(String.valueOf(subcategory));
        if (Objects.nonNull(thirdLevelCategory) && thirdLevelCategory != -1) {
            fileObject.setThirdLevelCategory(String.valueOf(thirdLevelCategory));
            fileObject.setAbility(FileCategoryAbilityAssociation.getAbilityByTagHistoryCategory(TagHistoryCategory.getByCode(thirdLevelCategory)));
        } else {
            fileObject.setAbility(FileCategoryAbilityAssociation.getAbilityBySubCategory(SubCategoryEnum.getByCode(subcategory)));
        }
        fileObjectService.updateById(fileObject);
    }

    private void updateFileParsed(Long fileId) {
        fileObjectService.updateFileParseStatus(fileId, FileStatus.PARSED);
    }

    @Override
    public Set<String> getSupportedTypes() {
        return Sets.newHashSet("xlsx", "xls", "csv");
    }


    /**
     * 文件处理
     *
     * @param file             文件
     * @param fileId           文件 ID
     * @param originalFilename 原始文件名
     * @param userId 用户id
     * @author luhao
     * @since 2025/06/25 19:09:11
     */
    private void doHandle(File file, Long fileId, String originalFilename, Long userId) {
        // 获取表头和数据总记录行数
        ExtraAttributesListener extraAttributesListener = new ExtraAttributesListener();
        ExcelReaderBuilder readerBuilder = EasyExcel.read(file, extraAttributesListener);
        String fileSuffix = FileUtils.getFileSuffix(originalFilename);
        setCsvFileEncoding(file, fileSuffix, readerBuilder);
        readerBuilder.sheet().headRowNumber(5).doRead();
        // 获取表头
        List<List<String>> excelHeaders = extraAttributesListener.getOriginalHeaders();
        // 获取行数
        int rowCount = extraAttributesListener.getRowCount();
        // 把表头转换为markdown
        String headerMarkdown = MarkdownConverter.generateMarkdownTable(excelHeaders);
        if (headerMarkdown == null) {
            log.warn("文件：{}表头转换为markdown为空", originalFilename);
            throw new ServerException("文件表头转换为markdown为空");

        }
        // 把文件进行分类
        FileClassifyResp fileClassifyResp = classifyFile(headerMarkdown, originalFilename);
        // 更新文件元数据
        updateFileParseMetadata(fileId, FileCategory.getValueByCode(fileClassifyResp.getCategory()), fileClassifyResp.getSummary(), fileClassifyResp.getSubcategory(), fileClassifyResp.getThird_level_category());
        // 通知解析进度【LLM分类成功】
        ProcessProgressSupport.notifyParseProcessing(fileId, userId,RandomUtil.getRandomPercentage(15, 20));
        // 根据业务分类找出业务处理器
        Optional<BusinessDataHandler> handlerOptional = businessDataHandlerFactory.getHandler(fileClassifyResp.getSubcategory());
        // 如果有业务处理器，则进行数据处理
        if (handlerOptional.isPresent()) {
            BusinessDataHandler businessDataHandler = handlerOptional.get();
            //  一些业务不需要LLM给出数据映射，直接处理
            if (businessDataHandler.isDirectHandler()) {
                businessDataHandler.processDirectly(file, fileId, rowCount);
                return;
            }
            // 获取大模型返回的实体映射
            FileAlignmentResp alignmentResp = getFileHeaderMapping(headerMarkdown, JSONUtil.toJsonStr(businessDataHandler.getDbSchemaDescription()), originalFilename);
            // 通知解析进程【LLM实体映射成功】
            ProcessProgressSupport.notifyParseProcessing(fileId,userId, RandomUtil.getRandomPercentage(30, 40));
            Map<String, String> mapping = JSONUtil.toBean(alignmentResp.getData(), new TypeReference<Map<String, String>>() {
            }, true);
            // 根据业务分类获取动态数据映射器
            DynamicMapper<Object, Object> dynamicMapper = mapperFactory.getMapper(fileClassifyResp.getSubcategory());
            // 创建Excel数据监听器
            ExcelDataListener excelDataListener = new ExcelDataListener(mapping, handlerOptional.get(), fileId, rowCount, dynamicMapper,userId);
            ExcelReaderBuilder read = EasyExcel.read(file, excelDataListener);
            setCsvFileEncoding(file, fileSuffix, read);
            read.sheet().doRead();
        } else { // 无业务处理器，则直接更新文件解析状态
            updateFileParsed(fileId);
            ProcessProgressSupport.notifyParseComplete(fileId,userId);

        }


    }

    private void setCsvFileEncoding(File file, String fileSuffix, ExcelReaderBuilder readerBuilder) {
        if ("csv".equals(fileSuffix)) {
            readerBuilder
                .excelType(ExcelTypeEnum.CSV)
                .charset(FileUtils.detectCharset(file));
        }
    }

    /**
     * 获取文件头到实体的映射
     *
     * @param excelHeaderMarkdown Excel 标题 Markdown
     * @param dataBaseSchema      数据库对应实体结构
     * @param originalFilename    原始文件名
     * @return {@link FileAlignmentResp }
     * @author luhao
     * @since 2025/06/26 11:29:50
     */
    private FileAlignmentResp getFileHeaderMapping(String excelHeaderMarkdown, String dataBaseSchema, String originalFilename) {
        FileAlignmentResp alignmentResp = llmFeign.alignment(FileAlignmentReq.builder()
            .excelHeader(excelHeaderMarkdown)
            .databaseSchema(dataBaseSchema)
            .subcategory(0)
            .documentType("")
            .build());
        if (Objects.isNull(alignmentResp)) {
            log.warn("文件：{}，LLM实体映射失败", originalFilename);
            throw new ServerException("文件实体映射失败");
        }
        log.info("文件：{}，LLM实体映射成功，结构体为:{}", originalFilename, JSONUtil.toJsonStr(alignmentResp.getData()));
        return alignmentResp;

    }

    /**
     * 文件分类
     *
     * @param headerMarkdown   Excel 表头（ markdown格式）
     * @param originalFilename 原始文件名
     * @return {@link FileClassifyResp }
     * @author luhao
     * @since 2025/06/25 11:27:39
     */
    private FileClassifyResp classifyFile(String headerMarkdown, String originalFilename) {
        FileClassifyResp classifyResp = llmFeign.classify(FileClassifyReq.builder()
            .headMarkdownContent(headerMarkdown)
            .documentType("excel").build());
        if (Objects.isNull(classifyResp)) {
            log.warn("文件：{}，LLM分类失败", originalFilename);
            throw new ServerException("文件分类失败");

        }
        return classifyResp;
    }


}
