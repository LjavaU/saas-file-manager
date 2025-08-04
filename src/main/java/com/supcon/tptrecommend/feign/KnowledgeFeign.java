package com.supcon.tptrecommend.feign;

import com.supcon.tptrecommend.feign.entity.knowledge.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@FeignClient(name = "knowledge", url = "${service.knowledge.address:知识库地址不能为空}", fallbackFactory = KnowledgeFeign.KnowledgeFeignFallBack.class)
public interface KnowledgeFeign {

    /**
     * 上传文件
     *
     * @param files    文件
     * @param userId   用户 ID
     * @param bucket   桶
     * @param object   对象
     * @param tenantId 租户 ID
     * @return {@link KnowledgeFileUploadResp }
     * @author luhao
     * @since 2025/07/29 15:13:15
     */
    @PostMapping(value = "/api/industry_domain_qa/saas/new/upload_files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    KnowledgeFileUploadResp<List<FileDataSimple>> uploadFiles(@RequestPart(value = "files") List<MultipartFile> files, @RequestPart(value = "user_id") String userId,
                                                              @RequestPart(value = "bucket") String bucket,
                                                              @RequestPart(value = "object") String object,
                                                              @RequestPart(value = "tenant_id") String tenantId);

    /**
     * 获取文件解析的状态
     *
     * @param userId   用户 ID
     * @param bucket   桶
     * @param object   对象
     * @param tenantId 租户 ID
     * @return {@link KnowledgeFileUploadResp }
     * @author luhao
     * @since 2025/07/29 15:43:19
     */
    @PostMapping(value = "/api/industry_domain_qa/saas/new/list_files")
    KnowledgeFileUploadResp<KnowledgeParseDetails> listFiles(@RequestPart(value = "user_id") String userId,
                                                             @RequestPart(value = "bucket") String bucket,
                                                             @RequestPart(value = "object") String object,
                                                             @RequestPart(value = "tenant_id") String tenantId);

    /**
     * 删除知识库
     *
     * @param userId   用户 ID
     * @param bucket   桶
     * @param object   对象
     * @param tenantId 租户 ID
     * @return {@link KnowledgeFileUploadResp }
     * @author luhao
     * @since 2025/07/29 15:48:33
     */
    @PostMapping(value = "/api/industry_domain_qa/saas/new/delete_knowledge_base")
    KnowledgeFileUploadResp deleteKnowledgeBase(@RequestPart(value = "user_id") String userId,
                               @RequestPart(value = "bucket") String bucket,
                               @RequestPart(value = "object") String object,
                               @RequestPart(value = "tenant_id") String tenantId);


    /**
     * 获取知识库文件的推荐问题
     *
     * @param req 请求体
     * @return {@link KnowledgeRecommendationResp }
     * @author luhao
     * @since 2025/08/01 16:15:44
     */
    @PostMapping(value = "/api/industry_domain_qa/saas/new/get_recommendation")
    KnowledgeFileUploadResp< List<String>> getRecommendation(@RequestBody KnowledgeRecommendationReq req);



    @Slf4j
    @Component
    class KnowledgeFeignFallBack implements FallbackFactory<KnowledgeFeign> {

        @Override
        public KnowledgeFeign create(Throwable cause) {
            return new KnowledgeFeign() {
                @Override
                public KnowledgeFileUploadResp<List<FileDataSimple>> uploadFiles(List<MultipartFile> files, String user_id, String bucket, String object, String tenant_id) {
                    log.error("/api/industry_domain_qa/saas/new/upload_files接口访问出错", cause);
                    return null;
                }

                @Override
                public KnowledgeFileUploadResp<KnowledgeParseDetails> listFiles(String userId, String bucket, String object, String tenantId) {
                    log.error("/api/industry_domain_qa/saas/new/list_files接口访问出错", cause);
                    return null;
                }


                @Override
                public KnowledgeFileUploadResp deleteKnowledgeBase(String user_id, String bucket, String object, String tenant_id) {
                    log.error("/api/industry_domain_qa/saas/new/delete_knowledge_base接口访问出错", cause);
                    return null;
                }

                @Override
                public KnowledgeFileUploadResp<List<String>> getRecommendation(KnowledgeRecommendationReq req) {
                    log.error("/api/industry_domain_qa/saas/new/get_recommendation接口访问出错", cause);
                    return null;
                }
            };
        }
    }

}
