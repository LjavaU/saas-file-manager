package com.example.saasfile.feign;

import com.example.saasfile.feign.entity.knowledge.*;
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

@FeignClient(name = "knowledge", url = "${service.knowledge.address:鐭ヨ瘑搴撳湴鍧€涓嶈兘涓虹┖}", fallbackFactory = KnowledgeFeign.KnowledgeFeignFallBack.class)
public interface KnowledgeFeign {

    
    @PostMapping(value = "/api/industry_domain_qa/saas/new/upload_files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    KnowledgeFileUploadResp<List<FileDataSimple>> uploadFiles(@RequestPart(value = "files") List<MultipartFile> files, @RequestPart(value = "user_id") String userId,
                                                              @RequestPart(value = "bucket") String bucket,
                                                              @RequestPart(value = "object") String object,
                                                              @RequestPart(value = "tenant_id") String tenantId);

    
    @PostMapping(value = "/api/industry_domain_qa/saas/new/list_files")
    KnowledgeFileUploadResp<KnowledgeParseDetails> listFiles(@RequestPart(value = "user_id") String userId,
                                                             @RequestPart(value = "bucket") String bucket,
                                                             @RequestPart(value = "object") String object,
                                                             @RequestPart(value = "tenant_id") String tenantId);

    
    @PostMapping(value = "/api/industry_domain_qa/saas/new/delete_knowledge_base")
    KnowledgeFileUploadResp deleteKnowledgeBase(@RequestPart(value = "user_id") String userId,
                               @RequestPart(value = "bucket") String bucket,
                               @RequestPart(value = "object") String object,
                               @RequestPart(value = "tenant_id") String tenantId);


    
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
                    log.error("/api/industry_domain_qa/saas/new/upload_files鎺ュ彛璁块棶鍑洪敊", cause);
                    return null;
                }

                @Override
                public KnowledgeFileUploadResp<KnowledgeParseDetails> listFiles(String userId, String bucket, String object, String tenantId) {
                    log.error("/api/industry_domain_qa/saas/new/list_files鎺ュ彛璁块棶鍑洪敊", cause);
                    return null;
                }


                @Override
                public KnowledgeFileUploadResp deleteKnowledgeBase(String user_id, String bucket, String object, String tenant_id) {
                    log.error("/api/industry_domain_qa/saas/new/delete_knowledge_base鎺ュ彛璁块棶鍑洪敊", cause);
                    return null;
                }

                @Override
                public KnowledgeFileUploadResp<List<String>> getRecommendation(KnowledgeRecommendationReq req) {
                    log.error("/api/industry_domain_qa/saas/new/get_recommendation鎺ュ彛璁块棶鍑洪敊", cause);
                    return null;
                }
            };
        }
    }

}
