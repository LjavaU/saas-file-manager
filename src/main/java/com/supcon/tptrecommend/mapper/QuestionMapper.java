package com.supcon.tptrecommend.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.supcon.system.base.entity.basic.IBaseMapper;
import com.supcon.tptrecommend.entity.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 问题主表 Mapper 接口
 * </p>
 *
 * @author luhao
 * @version 1.0.0
 * @date 2025-05-22
 */
@Mapper
public interface QuestionMapper extends IBaseMapper<Question> {
    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT id,\n" +
        "       tenant_id,\n" +
        "       user_id,\n" +
        "       industry,\n" +
        "       post,\n" +
        "       device,\n" +
        "       content\n" +
        "FROM question\n" +
        "WHERE (industry IS NULL AND device IS NULL AND device IS NULL)\n" +
        "  AND question.tenant_id = '0';")
    List<Question> listQuestionCommon();


}
