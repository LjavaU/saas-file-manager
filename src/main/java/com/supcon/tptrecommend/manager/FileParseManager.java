package com.supcon.tptrecommend.manager;

import java.io.File;

public interface FileParseManager {


    /**
     * 将文件解析为 Markdown
     *
     * @param file       文件
     * @param onlyHeader Only 标头
     * @return {@link String }
     * @throws Exception 异常
     * @author luhao
     * @since 2025/06/18 20:08:19
     */
    String parseFileToMarkdown(File file, Boolean onlyHeader) throws Exception;


}
