package com.supcon.tptrecommend.event;

import com.supcon.tptrecommend.event.entity.FileAnalysisEvent;
import com.supcon.tptrecommend.manager.FileManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileAnalysisListener {

    private final FileManager  fileManager;


    /**
     * 文件解析监听器
     *
     * @param event 事件
     * @author luhao
     * @date 2025/06/04 17:43:01
     */
    @EventListener
    public void handle(FileAnalysisEvent event) {
      fileManager.handleFileAnalysis(event.getFileId());

    }


}
