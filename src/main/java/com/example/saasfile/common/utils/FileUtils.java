package com.example.saasfile.common.utils;

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

    public static String formatFileSize(long size) {
        if (size <= 0) {
            return "0 B";
        }
        String[] units = {"B", "KB", "MB", "GB", "TB", "PB", "EB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        DecimalFormat df = new DecimalFormat("###0.##");
        return df.format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static Charset detectCharset(File file) {
        final int sampleSize = 64 * 1024;
        byte[] buf = new byte[4096];
        UniversalDetector detector = new UniversalDetector(null);
        int totalBytesRead = 0;
        try (FileInputStream fis = new FileInputStream(file)) {
            int nread;
            while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
                detector.handleData(buf, 0, nread);
                totalBytesRead += nread;
                if (totalBytesRead >= sampleSize) {
                    break;
                }
            }
        } catch (IOException e) {
            log.error("Charset detection failed", e);
            return StandardCharsets.UTF_8;
        }
        detector.dataEnd();
        String detectedCharsetName = detector.getDetectedCharset();
        detector.reset();

        if (detectedCharsetName != null) {
            try {
                return Charset.forName(detectedCharsetName);
            } catch (Exception e) {
                log.error("Unsupported charset: {}", detectedCharsetName, e);
                return StandardCharsets.UTF_8;
            }
        }
        return StandardCharsets.UTF_8;
    }

    public static boolean isKnowledgeDocumentFile(String originalFilename) {
        if (originalFilename == null) {
            return false;
        }
        return originalFilename.endsWith(".txt")
            || originalFilename.endsWith(".doc")
            || originalFilename.endsWith(".docx")
            || originalFilename.endsWith(".pdf");
    }

    public static String getFileNameFromObjectKey(String objectKey) {
        if (StrUtil.isBlank(objectKey)) {
            return null;
        }
        return objectKey.substring(objectKey.lastIndexOf("/") + 1);
    }

    public static String getOriginalFileNameFromObjectKey(String objectKey) {
        if (StrUtil.isBlank(objectKey)) {
            return "";
        }
        String fileName = objectKey.substring(objectKey.lastIndexOf("/") + 1);
        return fileName.substring(fileName.indexOf("_") + 1);
    }

    public static void deleteTemporaryFile(File file, String originalFilename) {
        if (file == null) {
            return;
        }
        try {
            Files.deleteIfExists(file.toPath());
        } catch (IOException e) {
            log.error("Failed to delete temporary file: {}", originalFilename, e);
        }
    }
}
