package com.supcon.tptrecommend.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.mozilla.universalchardet.UniversalDetector;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
     *
     * 通过流式处理检测文件编码，避免将整个文件读入内存。
     *
     * @param inputStream 文件的输入流。调用者负责关闭此流。
     * @return 检测到的 Charset。
     * @throws IOException 如果读取流时发生 I/O 错误。
     */
    public static Charset detectCharset(InputStream inputStream) throws IOException {
        UniversalDetector detector = new UniversalDetector(null);

        // 1. 创建一个缓冲区
        byte[] buffer = new byte[4096];
        int nread;

        // 2. 循环读取文件流，分块送入检测器
        while ((nread = inputStream.read(buffer)) > 0 && !detector.isDone()) {
            detector.handleData(buffer, 0, nread);
        }

        // 3. 标记数据结束
        detector.dataEnd();

        // 4. 获取结果并重置检测器
        String detectedCharsetName = detector.getDetectedCharset();
        detector.reset();

        if (detectedCharsetName != null) {
            try {
                return Charset.forName(detectedCharsetName);
            } catch (Exception e) {
                log.error("不支持的字符集名称: {}", detectedCharsetName, e);
                // 如果库返回了一个Java不支持的别名，则回退
                return StandardCharsets.UTF_8;
            }
        }

        // 如果检测失败，回退到默认值
        return StandardCharsets.UTF_8;
    }

    /**
     * 检测字符集
     *
     * @param bytes 字节
     * @return {@link Charset }
     * @throws IOException io异常
     * @author luhao
     * @since 2025/06/10 10:26:30
     */
    public static Charset detectCharset(byte[] bytes) throws IOException {
        try (InputStream in = new ByteArrayInputStream(bytes)){
           return detectCharset(in);
        }

    }

    /**
     * 【优化后的便捷方法】
     * 直接从 MultipartFile 检测编码，使用流式处理以支持大文件。
     *
     * @param file 上传的文件。
     * @return 检测到的 Charset.
     * @throws IOException 如果读取文件时发生 I/O 错误。
     */
    public static Charset detectCharset(MultipartFile file) throws IOException {
        // 使用 try-with-resources 确保 InputStream 在使用后被正确关闭
        try (InputStream inputStream = file.getInputStream()) {
            return detectCharset(inputStream);
        }
    }
}