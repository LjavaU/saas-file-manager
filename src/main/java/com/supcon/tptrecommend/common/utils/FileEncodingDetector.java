package com.supcon.tptrecommend.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 文件编码检测器
 *
 * @author luhao
 * @since 2025/06/19 09:36:08
 */
@Slf4j
public class FileEncodingDetector {


    /**
     * 检测文件中的字符集
     *
     * @param file 文件
     * @return {@link Charset }
     * @throws IOException io异常
     * @author luhao
     * @since 2025/06/19 09:34:52
     */
    public static Charset detectCharset(File file) throws IOException {
        // 1. 创建一个缓冲区
        byte[] buf = new byte[4096];
        UniversalDetector detector = new UniversalDetector(null);

        // 2. 循环读取文件流，分块送入检测器
        try (FileInputStream fis = new FileInputStream(file)) {
            int nread;
            while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
                detector.handleData(buf, 0, nread);
            }
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
                return StandardCharsets.UTF_8;
            }
        }
        // 如果检测失败，回退到默认值
        return StandardCharsets.UTF_8;
    }

}