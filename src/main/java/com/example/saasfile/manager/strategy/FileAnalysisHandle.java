package com.example.saasfile.manager.strategy;

import java.util.Set;

public interface FileAnalysisHandle {

    
    Set<String> getSupportedTypes();

    
    void handleFileAnalysis(Long fileId,Integer category);
}
