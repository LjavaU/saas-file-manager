package com.example.saasfile.service.impl;

import com.example.saasfile.support.mybatis.BasicServiceImpl;
import com.example.saasfile.dto.filerecommendation.FileRecommendationReq;
import com.example.saasfile.dto.filerecommendation.FileRecommendationResp;
import com.example.saasfile.entity.FileRecommendation;
import com.example.saasfile.mapper.FileRecommendationMapper;
import com.example.saasfile.service.IFileRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class FileRecommendationServiceImpl extends BasicServiceImpl<FileRecommendationMapper, FileRecommendation> implements IFileRecommendationService {

private final FileRecommendationMapper fileRecommendationMapper;

    
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
