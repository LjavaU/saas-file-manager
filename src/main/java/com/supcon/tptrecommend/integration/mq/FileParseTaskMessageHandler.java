package com.supcon.tptrecommend.integration.mq;

import cn.hutool.core.util.StrUtil;
import com.supcon.framework.mq.core.handler.AbstractMqMessageHandler;
import com.supcon.framework.tenant.core.getter.TenantContext;
import com.supcon.tptrecommend.common.config.FileParseMqProperties;
import com.supcon.tptrecommend.common.enums.FileStatus;
import com.supcon.tptrecommend.common.utils.ProcessProgressSupport;
import com.supcon.tptrecommend.dto.mq.FileParseTaskMessage;
import com.supcon.tptrecommend.entity.FileObject;
import com.supcon.tptrecommend.manager.strategy.FileAnalysisHandle;
import com.supcon.tptrecommend.manager.strategy.FileAnalysisHandleFactory;
import com.supcon.tptrecommend.service.IFileObjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileParseTaskMessageHandler extends AbstractMqMessageHandler<FileParseTaskMessage> {

    private final FileParseMqProperties fileParseMqProperties;

    private final FileAnalysisHandleFactory fileAnalysisHandleFactory;

    private final IFileObjectService fileObjectService;

    @Override
    public String binding() {
        return fileParseMqProperties.getFileParseBinding();
    }

    @Override
    public void handleMessage(FileParseTaskMessage message) {
        if (message == null || message.getFileId() == null) {
            log.error("Invalid parse task message: {}", message);
            return;
        }

        Long fileId = message.getFileId();
        Long userId = message.getUserId();
        try {
            if (StrUtil.isNotBlank(message.getTenantId())) {
                TenantContext.setCurrentTenant(message.getTenantId());
            }

            Optional<FileAnalysisHandle> handler = fileAnalysisHandleFactory.getHandler(FilenameUtils.getExtension(message.getOriginalFilename()));
            if (!handler.isPresent()) {
                updateFileParseStatus(fileId, FileStatus.PARSE_NOT_SUPPORT);
                notifyParseComplete(fileId, userId);
                return;
            }
            handler.get().handleFileAnalysis(fileId, message.getCategory());
        } catch (Exception ex) {
            log.error("File parse task consume failed. fileId={}", fileId, ex);
            updateFileParseStatus(fileId, FileStatus.PARSE_FAILED);
            notifyParseComplete(fileId, userId);
        } finally {
            TenantContext.clear();
        }
    }

    private void updateFileParseStatus(Long fileId, FileStatus status) {
        FileObject update = new FileObject();
        update.setId(fileId);
        update.setFileStatus(status.getValue());
        fileObjectService.updateById(update);
    }

    private void notifyParseComplete(Long fileId, Long userId) {
        Long targetUserId = Objects.nonNull(userId) ? userId : fileObjectService.getUserIdByFileId(fileId);
        if (targetUserId == null) {
            log.warn("Skip parse complete notify because userId is missing. fileId={}", fileId);
            return;
        }
        ProcessProgressSupport.notifyParseComplete(fileId, targetUserId);
    }
}