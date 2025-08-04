package com.supcon.tptrecommend.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.supcon.system.base.entity.basic.IBaseMapper;
import com.supcon.tptrecommend.dto.filerecommendation.FileRecommendationReq;
import com.supcon.tptrecommend.dto.filerecommendation.FileRecommendationResp;
import com.supcon.tptrecommend.entity.FileRecommendation;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 文件推荐问题生成 Mapper 接口
 * </p>
 *
 * @author luhao
 * @version 1.0.0
 * @date 2025-08-04
 */
@Mapper
public interface FileRecommendationMapper extends IBaseMapper<FileRecommendation> {
    /**
     * 获取关键词
     *
     * @param req 请求体
     * @return {@link String }
     * @author luhao
     * @since 2025/08/04 13:19:32
     */
    @InterceptorIgnore(tenantLine = "true")
    @Select("select id,tenant_id,file_id,keyword from file_recommendation where file_id = #{fileId} and tenant_id = #{tenantId}")
    FileRecommendationResp getKeyWord(FileRecommendationReq req);

    /**
     * 更新文件推荐
     *
     * @param fileRecommendation 文件推荐
     * @author luhao
     * @since 2025/08/04 14:03:17
     */
    @InterceptorIgnore(tenantLine = "true")
    @Update("update file_recommendation set questions = #{questions},update_time = #{updateTime}  where id = #{id}")
    void updateFileRecommend(FileRecommendation fileRecommendation);

    /**
     * 保存文件推荐
     *
     * @param fileRecommendation 文件推荐
     * @author luhao
     * @since 2025/08/04 14:03:19
     */
    @InterceptorIgnore(tenantLine = "true")
    @Insert("insert into file_recommendation (tenant_id,file_id,keyword,questions) values (#{tenantId},#{fileId},#{keyword},#{questions})")
    void saveFileRecommend(FileRecommendation fileRecommendation);
}
