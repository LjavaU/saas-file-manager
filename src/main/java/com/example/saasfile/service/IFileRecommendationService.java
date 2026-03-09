package com.example.saasfile.service;

import com.example.saasfile.support.mybatis.IBasicService;
import com.example.saasfile.dto.filerecommendation.FileRecommendationReq;
import com.example.saasfile.dto.filerecommendation.FileRecommendationResp;
import com.example.saasfile.entity.FileRecommendation;


public interface IFileRecommendationService extends IBasicService<FileRecommendation> {


    
    FileRecommendationResp getKeyWord(FileRecommendationReq req);

    
    void updateFileRecommend(FileRecommendation fileRecommendation);

    
    void saveFileRecommend(FileRecommendation fileRecommendation);
}
