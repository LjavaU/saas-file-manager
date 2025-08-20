package com.supcon.tptrecommend.manager.strategy.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Sets;
import com.supcon.tptrecommend.entity.FileObject;
import com.supcon.tptrecommend.manager.strategy.FileAnalysisHandle;
import com.supcon.tptrecommend.manager.strategy.KnowledgeFileHandleTemplate;
import com.supcon.tptrecommend.service.IFileObjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
        long count = fileObjectService.count(Wrappers.<FileObject>lambdaQuery()
            .eq(FileObject::getId, fileId));
        if (count < 1) {
            log.error("文件：{}的记录不存在", fileId);
            return;
        }
        // 处理知识库 文件
        knowledgeFileHandleTemplate.uploadToKnowledgeBase(fileId);
    }
}
