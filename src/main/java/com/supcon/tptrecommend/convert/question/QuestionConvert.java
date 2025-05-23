package com.supcon.tptrecommend.convert.question;

import com.supcon.tptrecommend.dto.question.QuestionCreateReq;
import com.supcon.tptrecommend.dto.question.QuestionResp;
import com.supcon.tptrecommend.dto.question.QuestionUpdateReq;
import com.supcon.tptrecommend.entity.Question;
import com.supcon.tptrecommend.entity.excel.QuestionImportExcel;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * <p>
 * 问题主表转换器
 * </p>
 *
 * @author luhao
 * @version 1.0.0
 * @date 2025-05-22
 */
@Mapper
public interface QuestionConvert {

    QuestionConvert INSTANCE = Mappers.getMapper(QuestionConvert.class);

    Question convert(QuestionCreateReq questionCreateReq);

    Question convert(QuestionUpdateReq questionUpdateReq);

    QuestionResp convert(Question question);

    List<Question> convertFromExcel(List<QuestionImportExcel> questions);
}