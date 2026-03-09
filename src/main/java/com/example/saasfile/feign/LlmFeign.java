package com.example.saasfile.feign;

import com.example.saasfile.feign.entity.llm.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(name = "llm", url = "${service.llm.address:澶фā鍨嬪湴鍧€涓嶈兘涓虹┖}", fallbackFactory = LlmFeign.LlmFeignFallBack.class)
public interface LlmFeign {

    @PostMapping(value = "/api/file/parsing")
    FileParseResp parse(@RequestBody FileParseReq fileParseReq);

    
    @PostMapping(value = "/api/file/class")
    FileClassifyResp classify(@RequestBody FileClassifyReq fileClassifyReq);


    
    @PostMapping(value = "/api/file/extract")
    FileExtractResp extract(@RequestBody FileExtractReq fileExtractReq);


    
    @PostMapping(value = "/api/file/alignment")
    FileAlignmentResp alignment(@RequestBody FileAlignmentReq fileAlignmentReq);


    
    @PostMapping(value = "/api/file/ner")
    Object ner(@RequestBody FileEquipmentExtractReq fileEquipmentExtractReq);


    @Slf4j
    @Component
    class LlmFeignFallBack implements FallbackFactory<LlmFeign> {

        @Override
        public LlmFeign create(Throwable cause) {
            return new LlmFeign() {
                @Override
                public FileParseResp parse(FileParseReq req) {
                    log.error("/api/file/parsing鎺ュ彛璁块棶鍑洪敊",cause);
                    return null;
                }


                @Override
                public FileClassifyResp classify(FileClassifyReq fileClassifyReq) {
                    log.error("/api/file/class鎺ュ彛璁块棶鍑洪敊",cause);
                    return null;
                }

                @Override
                public FileExtractResp extract(FileExtractReq fileExtractReq) {
                    log.error("/api/file/extract鎺ュ彛璁块棶鍑洪敊",cause);
                    return null;
                }

                @Override
                public FileAlignmentResp alignment(FileAlignmentReq fileAlignmentReq) {
                    log.error("/api/file/alignment鎺ュ彛璁块棶鍑洪敊",cause);
                    return null;
                }

                @Override
                public Object ner(FileEquipmentExtractReq fileEquipmentExtractReq) {
                    log.error("/api/file/ner鎺ュ彛璁块棶鍑洪敊",cause);
                    return null;
                }
            };
        }
    }


}
