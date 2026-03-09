package com.example.saasfile.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.example.saasfile.support.mybatis.IBaseMapper;
import com.example.saasfile.dto.filerecommendation.FileRecommendationReq;
import com.example.saasfile.dto.filerecommendation.FileRecommendationResp;
import com.example.saasfile.entity.FileRecommendation;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;


@Mapper
public interface FileRecommendationMapper extends IBaseMapper<FileRecommendation> {
    
    @InterceptorIgnore(tenantLine = "true")
    @Select("select id,tenant_id,file_id,keyword from file_recommendation where file_id = #{fileId} and tenant_id = #{tenantId}")
    FileRecommendationResp getKeyWord(FileRecommendationReq req);

    
    @InterceptorIgnore(tenantLine = "true")
    @Update("update file_recommendation set questions = #{questions},update_time = #{updateTime}  where id = #{id}")
    void updateFileRecommend(FileRecommendation fileRecommendation);

    
    @InterceptorIgnore(tenantLine = "true")
    @Insert("insert into file_recommendation (tenant_id,file_id,keyword,questions) values (#{tenantId},#{fileId},#{keyword},#{questions})")
    void saveFileRecommend(FileRecommendation fileRecommendation);
}
