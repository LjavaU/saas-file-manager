package com.supcon.tptrecommend.common.utils;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DecimalFormat;


@Slf4j
public class FileUtils {

    /**
     * 设置文件大小格式
     * 根据文件大小，动态选择单位
     *
     * @param size 大小
     * @return {@link String }
     * @author luhao
     * @since 2025/06/27 10:59:51
     */
    public static String formatFileSize(long size) {
        if (size <= 0) {
            return "0 B";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB", "PB", "EB"};
        // 计算文件大小的对数，以确定其所在的单位级别
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));

        // 使用 DecimalFormat 来格式化数字，保留两位小数
        DecimalFormat df = new DecimalFormat("###0.##");

        return df.format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    /**
     * 获取文件后缀
     *
     * @param originalFilename 原始文件名
     * @return {@link String }
     * @author luhao
     * @since 2025/06/19 10:18:57
     */
    public static String getFileSuffix(String originalFilename) {
        if (originalFilename == null) {
            return "";
        }

        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex == -1) {
            return "";
        }

        return originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
    }


    /**
     * 检测文件中的字符集
     *
     * @param file 文件
     * @return {@link Charset }
     * @author luhao
     * @since 2025/06/19 09:34:52
     */
    public static Charset detectCharset(File file) {
        // 定义一个合理的样本大小，例如 64KB。对于大多数文件来说绰绰有余。
        final int SAMPLE_SIZE = 64 * 1024;
        // 1. 创建一个缓冲区
        byte[] buf = new byte[4096];
        UniversalDetector detector = new UniversalDetector(null);

        // 用于计数已读取的总字节数
        int totalBytesRead = 0;
        // 2. 循环读取文件流，分块送入检测器
        try (FileInputStream fis = new FileInputStream(file)) {
            int nread;
            while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
                detector.handleData(buf, 0, nread);
                totalBytesRead += nread;
                // 如果读取的字节数已经超过我们设定的样本大小，就没必要继续了
                if (totalBytesRead >= SAMPLE_SIZE) {
                    break;
                }
            }
        } catch (IOException e) {
            log.error("文件编码检测异常", e);
            return StandardCharsets.UTF_8;
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


    /**
     * 判断是否是知识库文件的类型
     *
     * @param originalFilename 原始文件名
     * @return boolean
     * @author luhao
     * @since 2025/07/30 14:12:27
     */
    public static boolean isKnowledgeDocumentFile(String originalFilename) {
        if (originalFilename == null) {
            return false;
        }
        return originalFilename.endsWith(".txt") || originalFilename.endsWith(".doc") ||
            originalFilename.endsWith(".docx") || originalFilename.endsWith(".pdf");
    }

    /**
     * 从对象名称获取文件名
     *
     * @param objectName 对象名称
     * @return {@link String }
     * @author luhao
     * @since 2025/08/08 14:34:30
     *
     */
    public static String getFileNameFromObjectName(String objectName) {
        if (StrUtil.isBlank(objectName)) {
            return null;
        }
        return objectName.substring(objectName.lastIndexOf("/") + 1);
    }

    public static void deleteTemporaryFile(File file, String originalFilename) {
        if (file == null) {
            return;
        }
        try {
            Files.deleteIfExists(file.toPath());
        } catch (IOException e) {
            log.error("删除临时{}文件失败:", originalFilename, e);
        }
    }
}

