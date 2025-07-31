package com.supcon.tptrecommend.feign;

import com.supcon.tptrecommend.feign.entity.llm.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

/**
 * LLM  调用
 *
 * @author luhao
 * @date 2025/06/03 20:10:02
 */
@FeignClient(name = "llm", url = "${service.llm.address:大模型地址不能为空}", fallbackFactory = LlmFeign.LlmFeignFallBack.class)
public interface LlmFeign {

    @PostMapping(value = "/api/file/parsing")
    FileParseResp parse(@RequestBody FileParseReq fileParseReq);

    @PostMapping(value = "/api/file/convert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<Resource> convert(@RequestPart(value = "file") MultipartFile file);

    /**
     * 获取文件分类
     *
     * @param fileClassifyReq 文件分类请求体
     * @return {@link FileClassifyResp }
     * @author luhao
     * @since 2025/06/19 16:26:17
     */
    @PostMapping(value = "/api/file/class")
    FileClassifyResp classify(@RequestBody FileClassifyReq fileClassifyReq);


    /**
     * 文件内容提取
     *
     * @param fileExtractReq 文件提取请求体
     * @return {@link FileExtractResp }
     * @author luhao
     * @since 2025/06/19 16:33:15
     */
    @PostMapping(value = "/api/file/extract")
    FileExtractResp extract(@RequestBody FileExtractReq fileExtractReq);


    /**
     * 文件表头和实体映射对准
     * 注意：针对excel、csv文件
     *
     * @param fileAlignmentReq 文件对准请求体
     * @return {@link FileAlignmentResp }
     * @author luhao
     * @since 2025/06/26 11:18:16
     */
    @PostMapping(value = "/api/file/alignment")
    FileAlignmentResp alignment(@RequestBody FileAlignmentReq fileAlignmentReq);


    @Slf4j
    @Component
    class LlmFeignFallBack implements FallbackFactory<LlmFeign> {

        @Override
        public LlmFeign create(Throwable cause) {
            return new LlmFeign() {
                @Override
                public FileParseResp parse(FileParseReq req) {
                    log.error("/api/file/parsing接口访问出错",cause);
                    return null;
                }

                @Override
                public ResponseEntity<Resource> convert(MultipartFile multipartFile) {
                    log.error("/api/file/convert接口访问出错",cause);
                    return null;
                }

                @Override
                public FileClassifyResp classify(FileClassifyReq fileClassifyReq) {
                    log.error("/api/file/class接口访问出错",cause);
                    return null;
                }

                @Override
                public FileExtractResp extract(FileExtractReq fileExtractReq) {
                    log.error("/api/file/extract接口访问出错",cause);
                    return null;
                }

                @Override
                public FileAlignmentResp alignment(FileAlignmentReq fileAlignmentReq) {
                    log.error("/api/file/alignment接口访问出错",cause);
                    return null;
                }
            };
        }
    }


}
