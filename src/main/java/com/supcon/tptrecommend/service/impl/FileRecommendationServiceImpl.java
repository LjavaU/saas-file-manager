package com.supcon.tptrecommend.service.impl;

import com.supcon.system.base.entity.basic.impl.BasicServiceImpl;
import com.supcon.tptrecommend.dto.filerecommendation.FileRecommendationReq;
import com.supcon.tptrecommend.dto.filerecommendation.FileRecommendationResp;
import com.supcon.tptrecommend.entity.FileRecommendation;
import com.supcon.tptrecommend.mapper.FileRecommendationMapper;
import com.supcon.tptrecommend.service.IFileRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 文件推荐问题生成 服务实现类
 * </p>
 *
 * @author luhao
 * @version 1.0.0
 * @date 2025-08-04
 */
@Service
@RequiredArgsConstructor
public class FileRecommendationServiceImpl extends BasicServiceImpl<FileRecommendationMapper, FileRecommendation> implements IFileRecommendationService {

private final FileRecommendationMapper fileRecommendationMapper;

    /**
     * 获取关键词
     *
     * @param req 请求体
     * @return {@link String }
     * @author luhao
     * @since 2025/08/04 13:19:48
     */
    @Override
    public FileRecommendationResp getKeyWord(FileRecommendationReq req) {
       return fileRecommendationMapper.getKeyWord(req);
    }

    @Override
    public void updateFileRecommend(FileRecommendation fileRecommendation) {
        fileRecommendationMapper.updateFileRecommend(fileRecommendation);
    }

    @Override
    public void saveFileRecommend(FileRecommendation fileRecommendation) {
        fileRecommendationMapper.saveFileRecommend(fileRecommendation);

    }
}
