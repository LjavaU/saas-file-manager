package com.supcon.tptrecommend.job;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import com.supcon.framework.schedule.core.entity.ExecutorParam;
import com.supcon.framework.schedule.core.handler.AbstractJobHandler;
import com.supcon.tptrecommend.common.enums.KnowledgeParseState;
import com.supcon.tptrecommend.dto.fileobject.FileObjectResp;
import com.supcon.tptrecommend.entity.FileObject;
import com.supcon.tptrecommend.feign.KnowledgeFeign;
import com.supcon.tptrecommend.feign.entity.knowledge.FileDataSimple;
import com.supcon.tptrecommend.feign.entity.knowledge.KnowledgeFileUploadResp;
import com.supcon.tptrecommend.feign.entity.knowledge.KnowledgeParseDetails;
import com.supcon.tptrecommend.service.IFileObjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 知识库解析状态任务处理程序
 *
 * @author luhao
 * @since 2025/07/30 09:44:54
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KnowledgeParseStatusJobHandler extends AbstractJobHandler {

    private final KnowledgeFeign knowledgeFeign;

    private final IFileObjectService fileObjectService;

    /**
     * 具体执行方法
     *
     * @param userParam     用户参数
     * @param executorParam 执行参数
     */
    @Override
    public void execute(Map<String, Object> userParam, ExecutorParam executorParam) {
        List<FileObjectResp> knowledgeParsing = fileObjectService.getKnowledgeParsing();
        if (CollectionUtil.isEmpty(knowledgeParsing)) {
            return;
        }
        for (FileObjectResp fileObjectResp : knowledgeParsing) {
            KnowledgeFileUploadResp<KnowledgeParseDetails> parseDetails = knowledgeFeign.listFiles(String.valueOf(fileObjectResp.getUserId()), fileObjectResp.getBucketName(), fileObjectResp.getObjectName(), fileObjectResp.getTenantId());
            if (Objects.nonNull(parseDetails) && parseDetails.getCode() == HttpStatus.HTTP_OK) {
                KnowledgeParseDetails parseDetailsData = parseDetails.getData();
                if (Objects.nonNull(parseDetailsData)) {
                    List<FileDataSimple> details = parseDetailsData.getDetails();
                    if (CollectionUtil.isNotEmpty(details)) {
                        FileDataSimple fileDataSimple = details.get(0);
                        FileObject fileObject = new FileObject();
                        fileObject.setKnowledgeParseState(KnowledgeParseState.valueByDesc(fileDataSimple.getStatus()));
                        fileObject.setId(fileObjectResp.getId());
                        fileObject.setTenantId(fileObjectResp.getTenantId());
                        fileObject.setUpdateTime(LocalDateTime.now());
                        fileObjectService.updateKnowledgeParseState(fileObject);
                    }
                }

            } else {
                log.error("获取知识库文件解析状态失败：{}", JSONUtil.parse(parseDetails));
            }

        }
    }

    @Override
    public String name() {
        return "knowledgeParseStatusJob";
    }

    /**
     * 处理消息发生异常时调用，默认不做任何处理
     *
     * @param userParam     用户参数
     * @param executorParam 执行参数
     * @param e             异常
     */
    @Override
    public void handlerException(Map<String, Object> userParam, ExecutorParam executorParam, Exception e) {
        log.error("任务执行异常", e);
    }
}