package com.supcon.tptrecommend.manager.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.supcon.systemcommon.entity.SupRequestBody;
import com.supcon.systemcommon.entity.SupResult;
import com.supcon.tptrecommend.common.WebsocketPush;
import com.supcon.tptrecommend.common.utils.ProcessProgressSupport;
import com.supcon.tptrecommend.dto.FileParse.FileParseProgressResp;
import com.supcon.tptrecommend.entity.FileObject;
import com.supcon.tptrecommend.feign.LlmFeign;
import com.supcon.tptrecommend.feign.TempLabelFeign;
import com.supcon.tptrecommend.feign.entity.FileParseReq;
import com.supcon.tptrecommend.feign.entity.FileParseResp;
import com.supcon.tptrecommend.feign.entity.TmpLabelComponentCreateReq;
import com.supcon.tptrecommend.feign.entity.TmpLabelDeviceCreateReq;
import com.supcon.tptrecommend.manager.FileAnalysisHandle;
import com.supcon.tptrecommend.service.IFileObjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class WordFileAnalysishandle implements FileAnalysisHandle {
    private final RestTemplate restTemplate = new RestTemplate();
    private final IFileObjectService fileObjectService;
    private final LlmFeign llmFeign;
    private final TempLabelFeign tempLabelFeign;

    @Value("${service.llm.address:localhost}")
    private String llmUrl;

    @Override
    public void handleFileAnalysis(byte[] bytes, Long fileId) {
        FileObject fileObject = fileObjectService.getById(fileId);
        if (fileObject != null) {
            String originalName = fileObject.getOriginalName();
            ResponseEntity<byte[]> responseEntity = callLlmApiWithFile(bytes, originalName);
            if (responseEntity != null) {
                if (!responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null) {
                    log.error("{}文件调用py接口转换markdown有误", originalName);
                    return;
                }
                byte[] fileBytes = responseEntity.getBody();
                // 2. 确定字符编码
                Charset charset = getCharsetFromResponse(responseEntity);
                // 3. 使用确定的编码将字节数组转换为字符串
                String content = new String(fileBytes, charset);
                doAnalysis(content, fileId);
            }
        }


    }

    private void doAnalysis(String content, Long fileId) {
        final int segmentSize = 1024;
        // 2. 使用数学计算和循环来分段
        int totalSegments = (int) Math.ceil((double) content.length() / segmentSize);
        if (totalSegments == 0) {
            return;
        }

        String headMarkdownContent = content.substring(0, Math.min(segmentSize, content.length()));
        String previousSegment = ""; // 初始化为空字符串，用于处理第一段
        int lastReportedProgress = 0;
        // 存储结果的数组
        JSONArray resultArray = new JSONArray();
        String category = "";
        String summary = "";
        for (int i = 0; i < totalSegments; i++) {
            // TODO:// 限制段数
            if (i == 2) {
                break;
            }
            // 推送分段进度
            int Progress = recordTaskProgress(i, totalSegments, fileId, lastReportedProgress);
            int start = i * segmentSize;
            int end = Math.min((i + 1) * segmentSize, content.length());
            String currentSegment = content.substring(start, end);
            log.info("LLM正在分析word文档...,一共{}段，正在处理第 {} 段...", segmentSize, i + 1);
            // 4. 增加异常处理
            try {
                FileParseReq request = FileParseReq.builder()
                    .headMarkdownContent(headMarkdownContent)
                    .markdownContent(currentSegment)
                    .previousMarkdownContent(previousSegment)
                    .documentType("doc")
                    .build();
                FileParseResp parse = llmFeign.parse(request);
                if (parse != null && parse.getData() != null) {
                    category = parse.getCategory();
                    summary = parse.getSummary();
                    JSONArray data = parse.getData();
                    resultArray.addAll(data);
                    log.info("LLM正在分析word文档...,第{}段，处理完成...", i + 1);
                }
            } catch (Exception e) {
                log.error("处理第 {} 段时发生异常: {}", i + 1, e);

            }
            // 5. 在循环的最后，更新 "previousSegment" 以供下一次循环使用
            previousSegment = currentSegment;
            lastReportedProgress = Progress;
        }
        if (resultArray.isEmpty()) {
            updateFileStatus(fileId, FileObject.FileStatus.PARSE_FAILED.getValue());
            ProcessProgressSupport.notifyParseComplete(fileId);
            return;
        } else {
            updateFileParseSuccess(fileId, FileObject.Category.getValueByCode(category), summary);
            ProcessProgressSupport.notifyParseComplete(fileId);
        }
        Set<TmpLabelDeviceCreateReq> devices = Sets.newHashSet();
        buildData(resultArray, devices);
        if (!devices.isEmpty()) {
            SupResult<Boolean> supResult = tempLabelFeign.addDevice(SupRequestBody.data(new ArrayList<>(devices)));
            if (supResult.getSuccess()) {
                log.info("装置数据保存成功");
            } else {
                log.error("装置数据保存失败:{}", supResult.getMsg());
            }

        }

    }

    private void updateFileStatus(Long fileId, Integer status) {
        FileObject fileObject = new FileObject();
        fileObject.setId(fileId);
        fileObject.setFileStatus(status);
        fileObjectService.updateById(fileObject);
    }

    private void updateFileParseSuccess(Long fileId, String category, String summary) {
        FileObject fileObject = new FileObject();
        fileObject.setId(fileId);
        fileObject.setCategory(category);
        fileObject.setContentOverview(summary);
        fileObject.setFileStatus(FileObject.FileStatus.PARSED.getValue());
        fileObjectService.updateById(fileObject);
    }


    public void buildData(JSONArray dataArray, Set<TmpLabelDeviceCreateReq> devices) {
        if (CollectionUtil.isEmpty(dataArray)) {
            return;
        }
        JSONObject jsonObject = dataArray.getJSONObject(0);
        if (jsonObject.containsKey("操作规程") && jsonObject.containsKey("子环节") && jsonObject.containsKey("装置组")) {
            for (Object sourceObj : dataArray) {
                JSONObject jsObj = (JSONObject) sourceObj;
                JSONArray deviceArray = jsObj.getJSONArray("装置组");
                if (CollectionUtil.isNotEmpty(deviceArray)) {
                    deviceArray.forEach(o -> {
                        JSONObject deviceObject = (JSONObject) o;
                        TmpLabelDeviceCreateReq tmpLabelDeviceCreateReq = new TmpLabelDeviceCreateReq();
                        tmpLabelDeviceCreateReq.setDeviceName(deviceObject.getStr("装置名称"));
                        tmpLabelDeviceCreateReq.setDeviceTag(deviceObject.getStr("装置位号"));
                        tmpLabelDeviceCreateReq.setDeviceId(RandomUtil.randomInt());
                        devices.add(tmpLabelDeviceCreateReq);
                    });
                }
                JSONArray subLinks = jsObj.getJSONArray("子环节");
                for (Object subLink : subLinks) {
                    JSONObject subLinkObject = (JSONObject) subLink;
                    // 获取子环节的装置组
                    JSONArray sublinkDeviceArray = subLinkObject.getJSONArray("装置组");
                    if (CollectionUtil.isNotEmpty(sublinkDeviceArray)) {
                        sublinkDeviceArray.forEach(o -> {
                            JSONObject deviceObject = (JSONObject) o;
                            TmpLabelDeviceCreateReq tmpLabelDeviceCreateReq = new TmpLabelDeviceCreateReq();
                            tmpLabelDeviceCreateReq.setDeviceName(deviceObject.getStr("装置名称"));
                            tmpLabelDeviceCreateReq.setDeviceTag(deviceObject.getStr("装置位号"));
                            tmpLabelDeviceCreateReq.setDeviceId(RandomUtil.randomInt());
                            devices.add(tmpLabelDeviceCreateReq);
                        });
                    }
                    JSONArray sublinksArray = subLinkObject.getJSONArray("子环节");
                    buildData(sublinksArray, devices);
                }

            }
        } else if (jsonObject.containsKey("工艺指标名") && jsonObject.containsKey("指标类型") && jsonObject.containsKey("组分")) {
            List<TmpLabelComponentCreateReq> componentCreateReqs = Lists.newArrayList();
            for (Object o : dataArray) {
                JSONObject componentObj = (JSONObject) o;
                JSONArray ComponentArray = componentObj.getJSONArray("组分");
                if (CollectionUtil.isEmpty(ComponentArray)) {
                    continue;
                }
                for (Object object : ComponentArray) {
                    JSONObject componentObject = (JSONObject) object;
                    TmpLabelComponentCreateReq tmpLabelComponentCreateReq = new TmpLabelComponentCreateReq();
                    tmpLabelComponentCreateReq.setCompName(componentObject.getStr("组分名"));
                    tmpLabelComponentCreateReq.setCompRatio(componentObject.getFloat("组成"));
                    tmpLabelComponentCreateReq.setCompId(RandomUtil.randomInt());
                    componentCreateReqs.add(tmpLabelComponentCreateReq);
                }
            }
            if (!componentCreateReqs.isEmpty()) {
                SupResult<Boolean> supResult = tempLabelFeign.addComponent(SupRequestBody.data(componentCreateReqs));
                if (supResult.getSuccess()) {
                    log.info("组分数据保存成功");
                } else {
                    log.error("组分保存失败");
                }
            } else {
                log.error("组分数据为空");
            }


        } /*else if (jsonObject.containsKey("工艺指标名") && jsonObject.containsKey("指标类型") && jsonObject.containsKey("项组")) {
            JSONArray itemArray = jsonObject.getJSONArray("项组");
            List<TmpLabelTargetCreateReq> labelTargetCreateReqs = Lists.newArrayList();
            for (Object o : itemArray) {
                JSONObject itemObj = (JSONObject) o;
                JSONArray childItemArray = itemObj.getJSONArray("子项组");
                for (Object object : childItemArray) {
                    JSONObject childItemObj = (JSONObject) object;
                    TmpLabelTargetCreateReq tmpLabelTargetCreateReq = new TmpLabelTargetCreateReq();
                    tmpLabelTargetCreateReq.setTargetName(childItemObj.getStr("子项名"));
                    tmpLabelTargetCreateReq.setTargetDesc(childItemObj.getStr("项描述"));
                    labelTargetCreateReqs.add(tmpLabelTargetCreateReq);

                }
            }
          *//*  if (!labelTargetCreateReqs.isEmpty()) {
                SupResult<Boolean> supResult = tempLabelFeign.addItem(SupRequestBody.data(labelTargetCreateReqs));
                if (supResult.getSuccess()) {
                    log.info("子项列表保存成功");
                } else {
                    log.error("子项列表保存失败");
                }
            }*//*

        }*/
    }

    /**
     * 模拟一个任务的处理进度
     *
     * @param i                    任务索引
     * @param totalSteps           总步数
     * @param fileId               文件 ID
     * @param lastReportedProgress 上次报告进度
     * @return int
     * @author luhao
     * @since 2025/06/13 14:15:49
     */
    private int recordTaskProgress(int i, int totalSteps, Long fileId, Integer lastReportedProgress) {
        // 2. 计算真实的“基础进度”
        int baseProgress = (int) (((double) (i + 1) / totalSteps) * 100);
        Random random = new Random();
        // 3. 增加随机性，但要保证进度是递增的
        // 生成一个0到4之间的小的随机增量
        int randomJitter = random.nextInt(5);
        int simulatedProgress = baseProgress + randomJitter;

        // 4. 确保新进度不小于上一次的进度，且不超过99
        simulatedProgress = Math.max(simulatedProgress, lastReportedProgress);
        simulatedProgress = Math.min(simulatedProgress, 99);

        // 5. 更新进度
        WebsocketPush.pushMessage(FileParseProgressResp.builder()
            .fileId(fileId)
            .parseProgress(simulatedProgress)
            .build());
        return simulatedProgress; // 记录本次进度
    }

    /**
     * 从HTTP响应头中解析Content-Type以获取字符集。
     *
     * @param responseEntity 响应实体
     * @return 解析出的字符集，如果未指定则默认为UTF-8
     */
    private Charset getCharsetFromResponse(ResponseEntity<?> responseEntity) {
        MediaType contentType = responseEntity.getHeaders().getContentType();
        if (contentType != null && contentType.getCharset() != null) {
            return contentType.getCharset();
        }
        // 如果响应头没有指定编码，提供一个安全可靠的默认值
        return StandardCharsets.UTF_8;
    }

    @Override
    public Set<String> getSupportedTypes() {
        return Sets.newHashSet("doc", "docx");
    }

    private ResponseEntity<byte[]> callLlmApiWithFile(byte[] fileBytes, String originalFilename) {
        String url = llmUrl + "api/file/convert"; // 替换为你的Python接口地址

        // 1. 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // 2. 将字节数组包装成资源，并重写getFilename方法以保留原始文件名
        ByteArrayResource fileResource = new ByteArrayResource(fileBytes) {
            @Override
            public String getFilename() {
                return originalFilename;
            }
        };

        // 3. 构建 multipart/form-data 请求体
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        // "file" 必须与Python接口中定义的接收文件的参数名一致
        body.add("file", fileResource);
        // 4. 创建HttpEntity，它包含请求头和请求体
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        // 5. 发送POST请求
        try {
            return restTemplate.postForEntity(url, requestEntity, byte[].class);
        } catch (Exception e) {
            log.error("调用llm接口：api/file/convert访问出错 ", e);
        }

        return null;
    }
}
