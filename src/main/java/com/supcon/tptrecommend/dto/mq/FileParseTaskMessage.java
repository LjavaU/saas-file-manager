package com.supcon.tptrecommend.dto.mq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileParseTaskMessage {

    private Long fileId;

    private Long userId;

    private String originalFilename;

    private Integer category;

    private String tenantId;
}