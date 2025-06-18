package com.supcon.tptrecommend.manager;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface FileParseManager {
    /**
     * 将文件解析为 Markdown
     *
     * @param file       文件
     * @param onlyHeader 是否只包含标题
     * @return {@link String } 返回Markdown字符串
     * @throws Exception 异常
     * @author luhao
     * {@code @date} 2025/06/09 15:41:38
     * @since 2025/06/09 16:42:23
     */
    String parseFileToMarkdown(MultipartFile file,Boolean onlyHeader) throws Exception;

    /**
     * 将字节流解析为 Markdown
     *
     * @param bytes            字节
     * @param originalFilename 原始文件名
     * @param onlyHeader       是否只包含标题
     * @return {@link String } 返回Markdown字符串
     * @throws Exception 异常
     * @author luhao
     * @since 2025/06/09 16:29:08
     */
    String parseBytesToMarkdown(byte[] bytes,  String originalFilename,Boolean onlyHeader) throws Exception;


    String parseFileToMarkdown(File file, Boolean onlyHeader) throws Exception;


}
