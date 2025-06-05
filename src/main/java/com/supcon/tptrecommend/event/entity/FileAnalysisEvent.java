package com.supcon.tptrecommend.event.entity;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.context.ApplicationEvent;

@Accessors(chain = true)
@Getter
public class FileAnalysisEvent extends ApplicationEvent {

    private final Long  fileId;

    public FileAnalysisEvent(Object source, Long fileId) {
        super(source);
        this.fileId = fileId;
    }




}
