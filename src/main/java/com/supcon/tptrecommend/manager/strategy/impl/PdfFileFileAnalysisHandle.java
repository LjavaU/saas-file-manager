package com.supcon.tptrecommend.manager.strategy.impl;

import com.google.common.collect.Sets;
import com.supcon.framework.tenant.core.getter.TenantContext;
import com.supcon.tptrecommend.common.utils.ProcessProgressSupport;
import com.supcon.tptrecommend.common.utils.RandomUtil;
import com.supcon.tptrecommend.entity.FileObject;
import com.supcon.tptrecommend.manager.strategy.FileAnalysisHandle;
import com.supcon.tptrecommend.manager.strategy.KnowledgeFileHandleTemplate;
import com.supcon.tptrecommend.service.IFileObjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;

/**
 * PDF 文件处理器
 *
 * @author luhao
 * @since 2025/08/08 13:28:05
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PdfFileFileAnalysisHandle implements FileAnalysisHandle {

    private final KnowledgeFileHandleTemplate knowledgeFileHandleTemplate;

    private final IFileObjectService fileObjectService;

    @Override
    public Set<String> getSupportedTypes() {
        return Sets.newHashSet("pdf", "txt");
    }

    @Override
    public void handleFileAnalysis(Long fileId, Integer category) {
        log.warn("handleFileAnalysis - 当前租户: {}", TenantContext.getCurrentTenant());
        FileObject fileObject = fileObjectService.getById(fileId);
        if (Objects.isNull(fileObject)) {
            log.error("PDF文件解析过程，文件：{}的记录不存在", fileId);
            return;
        }
        // 通知解析进度
        Long userId = fileObject.getUserId();
        ProcessProgressSupport.notifyParseProcessing(fileId, userId, RandomUtil.getRandomPercentage(5, 10));
        // 处理知识库 文件
        knowledgeFileHandleTemplate.uploadToKnowledgeBase(fileId, fileObject.getObjectName(), fileObject.getBucketName(), fileObject.getFileSize(), userId);
    }
}
