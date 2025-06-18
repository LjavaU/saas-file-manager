package com.supcon.tptrecommend.manager.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.google.common.collect.Sets;
import com.supcon.systemcommon.entity.SupRequestBody;
import com.supcon.systemcommon.entity.SupResult;
import com.supcon.tptrecommend.common.utils.ProcessProgressSupport;
import com.supcon.tptrecommend.entity.FileObject;
import com.supcon.tptrecommend.feign.DataHubFeign;
import com.supcon.tptrecommend.feign.LlmFeign;
import com.supcon.tptrecommend.feign.entity.*;
import com.supcon.tptrecommend.manager.FileAnalysisHandle;
import com.supcon.tptrecommend.manager.FileParseManager;
import com.supcon.tptrecommend.service.IFileObjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExcelFileAnalysishandle implements FileAnalysisHandle {
    private final IFileObjectService fileObjectService;
    private final FileParseManager fileParseManager;
    private final DataHubFeign dataHubFeign;
    private final LlmFeign llmFeign;
    public static final Set<Long> STOP_SIGNAL_CACHE = Sets.newConcurrentHashSet();

    @Override
    public void handleFileAnalysis(byte[] bytes, Long fileId) {
        FileObject fileObject = fileObjectService.getById(fileId);
        String originalFilename = fileObject.getOriginalName();
        if (FileObject.FileStatus.UNPARSED.getValue().equals(fileObject.getFileStatus())) {
            // 模拟的方式推送处理进度
            ProcessProgressSupport.notifyProcessProgress(fileId);
            String fullContentMarkdown;
            String headMarkdown;
            try {
                fullContentMarkdown = fileParseManager.parseBytesToMarkdown(bytes, originalFilename, false);
                headMarkdown = fileParseManager.parseBytesToMarkdown(bytes, originalFilename, true);
            } catch (Exception e) {
                log.error("文件{}解析失败：", originalFilename, e);
                updateFileStatus(fileId, FileObject.FileStatus.PARSE_FAILED.getValue());
                ProcessProgressSupport.notifyParseComplete(fileId);
                return;
            }
            if (StrUtil.isAllNotBlank(fullContentMarkdown, headMarkdown)) {
                parseWithLLM(fileId, fullContentMarkdown, originalFilename, headMarkdown);
            } else {
                updateFileStatus(fileId, FileObject.FileStatus.PARSE_FAILED.getValue());
                log.info("文件：{}，转换为markdown内容为空", originalFilename);
                ProcessProgressSupport.notifyParseComplete(fileId);
            }


        }
    }

    private void updateFileStatus(Long fileId, Integer status) {
        FileObject fileObject = new FileObject();
        fileObject.setId(fileId);
        fileObject.setFileStatus(status);
        fileObjectService.updateById(fileObject);
    }

    private void parseWithLLM(Long fileId, String markdown, String originalFilename, String headMarkdown) {
        FileParseResp parse = llmFeign.parse(FileParseReq.builder()
            .markdownContent(markdown)
            .headMarkdownContent(headMarkdown)
            .documentType("excel")
            .previousMarkdownContent("")
            .build());
        if (parse != null) {
            STOP_SIGNAL_CACHE.add(fileId);
            String category = FileObject.Category.getValueByCode(parse.getCategory());
            updateFileParseSuccess(fileId, category, parse.getSummary());
            ProcessProgressSupport.notifyParseComplete(fileId);
            try {
                buildDataAndSave(parse.getData(), originalFilename);
            } catch (Exception e) {
                log.error("excel文档类数据保存失败", e);
            }

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

    @Override
    public Set<String> getSupportedTypes() {
        return Sets.newHashSet("xlsx", "xls", "csv");
    }

    @Override
    public void handleFileAnalysis(String filePath, Long fileId) {
        FileObject fileObject = fileObjectService.getById(fileId);
        String originalFilename = fileObject.getOriginalName();
        if (FileObject.FileStatus.UNPARSED.getValue().equals(fileObject.getFileStatus())) {
            // 模拟的方式推送处理进度
            ProcessProgressSupport.notifyProcessProgress(fileId);
            String fullContentMarkdown;
            String headMarkdown;
            try {
                File file = new File(filePath);
                fullContentMarkdown = fileParseManager.parseFileToMarkdown(file, false);
                headMarkdown = fileParseManager.parseFileToMarkdown(file, true);
            } catch (Exception e) {
                log.error("文件{}解析失败：", originalFilename, e);
                updateFileStatus(fileId, FileObject.FileStatus.PARSE_FAILED.getValue());
                ProcessProgressSupport.notifyParseComplete(fileId);
                return;
            } finally {
                try {
                    Files.deleteIfExists(Paths.get(filePath));
                } catch (IOException e) {
                    log.error("删除临时CSV文件失败: {}", filePath, e);
                }
            }
            if (StrUtil.isAllNotBlank(fullContentMarkdown, headMarkdown)) {
                parseWithLLM(fileId, fullContentMarkdown, originalFilename, headMarkdown);
            } else {
                updateFileStatus(fileId, FileObject.FileStatus.PARSE_FAILED.getValue());
                log.info("文件：{}，转换为markdown内容为空", originalFilename);
                ProcessProgressSupport.notifyParseComplete(fileId);
            }


        }
    }
}
