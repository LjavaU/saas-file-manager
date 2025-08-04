package com.supcon.tptrecommend.service;

import com.supcon.system.base.entity.basic.IBasicService;
import com.supcon.tptrecommend.dto.filerecommendation.FileRecommendationReq;
import com.supcon.tptrecommend.dto.filerecommendation.FileRecommendationResp;
import com.supcon.tptrecommend.entity.FileRecommendation;

/**
 * <p>
 * 文件推荐问题生成 服务类
 * </p>
 *
 * @author luhao
 * @version 1.0.0
 * @date 2025-08-04
 */
public interface IFileRecommendationService extends IBasicService<FileRecommendation> {


    /**
     * 获取关键词
     *
     * @param req 请求体
     * @return {@link String }
     * @author luhao
     * @since 2025/08/04 13:19:53
     */
    FileRecommendationResp getKeyWord(FileRecommendationReq req);

    /**
     * 更新文件推荐
     *
     * @param fileRecommendation 文件推荐
     * @author luhao
     * @since 2025/08/04 13:49:25
     */
    void updateFileRecommend(FileRecommendation fileRecommendation);

    /**
     * 保存文件推荐
     *
     * @param fileRecommendation 文件推荐
     * @author luhao
     * @since 2025/08/04 13:49:26
     */
    void saveFileRecommend(FileRecommendation fileRecommendation);
}
