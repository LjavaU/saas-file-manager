package com.supcon.tptrecommend.manager.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

@Component
@RequiredArgsConstructor
public class FileAnalysisHandleFactory {

    private final List<FileAnalysisHandle> fileAnalysisHandles;

    private final Map<String, FileAnalysisHandle> strategyMap = new HashMap<>();


    @PostConstruct
    public void init() {
        if (fileAnalysisHandles != null) {
            for (FileAnalysisHandle handler : fileAnalysisHandles) {
                Set<String> supportedTypes = handler.getSupportedTypes();
                if (supportedTypes != null) {
                    for (String type : supportedTypes) {
                        // 将文件类型转换为大写，以便进行不区分大小写的查找
                        strategyMap.put(type.toUpperCase(), handler);
                    }
                }
            }
        }
    }

    /**
     * 根据文件类型（后缀）获取对应的处理器。
     * @param fileType 文件后缀，例如 "xls", "docx"
     * @return 对应的处理器Optional
     */
    public Optional<FileAnalysisHandle> getHandler(String fileType) {
        if (fileType == null || fileType.isEmpty()) {
            return Optional.empty();
        }
        // 使用大写进行查找，实现不区分大小写
        FileAnalysisHandle value = strategyMap.get(fileType.toUpperCase());
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(value);
    }
}
