package com.example.saasfile.manager.strategy.impl;

import com.google.common.collect.Sets;
import com.example.saasfile.common.utils.ProcessProgressSupport;
import com.example.saasfile.common.utils.RandomUtil;
import com.example.saasfile.entity.FileObject;
import com.example.saasfile.manager.strategy.FileAnalysisHandle;
import com.example.saasfile.manager.strategy.KnowledgeFileHandleTemplate;
import com.example.saasfile.service.IFileObjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;


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
        FileObject fileObject = fileObjectService.getById(fileId);
        if (Objects.isNull(fileObject)) {
            log.error("PDF parse record not found. fileId={}", fileId);
            return;
        }
        Long userId = fileObject.getUserId();
        ProcessProgressSupport.notifyParseProcessing(fileId, userId, RandomUtil.getRandomPercentage(5, 10));
        knowledgeFileHandleTemplate.uploadToKnowledgeBase(fileId, fileObject.getObjectName(), fileObject.getBucketName(), fileObject.getFileSize(), userId);
    }
}
