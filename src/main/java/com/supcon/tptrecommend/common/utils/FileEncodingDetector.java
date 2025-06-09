package com.supcon.tptrecommend.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.mozilla.universalchardet.UniversalDetector;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 文件编码检测器
 *
 * @author luhao
 * @date 2025/06/09 15:43:50
 */
@Slf4j
public class FileEncodingDetector {

    /**
     * 检测文件流的字符编码。
     *
     *
     * @param bytes 文件内容的字节数组.
     * @return 检测到的 Charset，如果检测失败，则为默认值 （UTF-8）。
     */
    public static Charset detectCharset(byte[] bytes) {
        UniversalDetector detector = new UniversalDetector(null);

        // handleData() a chunk of the data and check if the detector has reached a conclusion
        detector.handleData(bytes, 0, bytes.length);
        
        // Mark the end of the data
        detector.dataEnd();

        String detectedCharsetName = detector.getDetectedCharset();
        
        detector.reset();

        if (detectedCharsetName != null) {
            try {
                return Charset.forName(detectedCharsetName);
            } catch (Exception e) {
                log.error("不支持的字符集名称: {}", detectedCharsetName);
                return StandardCharsets.UTF_8;
            }
        }
        
        // Fallback to a default charset if detection fails
        return StandardCharsets.UTF_8;
    }

    /**
     *直接从 MultipartFile 检测编码的便捷方法。
     * 注意：这会将整个文件读入内存。对于超大文件，
     * 如果内存是一个问题，请考虑使用流式处理方法。
     *
     * @param file 上传的文件。
     * @return 检测到的 Charset.
     * @throws IOException if an I/O error occurs reading the file.
     */
    public static Charset detectCharset(MultipartFile file) throws IOException {
        return detectCharset(file.getBytes());
    }
}