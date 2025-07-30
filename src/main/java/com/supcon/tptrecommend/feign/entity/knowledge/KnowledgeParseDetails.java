package com.supcon.tptrecommend.feign.entity.knowledge;

import lombok.Data;

import java.util.List;

/**
 * 知识解析详细信息
 *
 * @author luhao
 * @since 2025/07/30 10:31:52
 */
@Data
public class KnowledgeParseDetails {

    private List<FileDataSimple> details;
}
