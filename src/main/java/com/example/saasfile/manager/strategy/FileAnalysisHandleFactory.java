package com.example.saasfile.manager.strategy;

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
                        strategyMap.put(type.toUpperCase(), handler);
                    }
                }
            }
        }
    }

    
    public Optional<FileAnalysisHandle> getHandler(String fileType) {
        if (fileType == null || fileType.isEmpty()) {
            return Optional.empty();
        }
        FileAnalysisHandle value = strategyMap.get(fileType.toUpperCase());
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(value);
    }
}
