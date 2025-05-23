package com.supcon.tptrecommend.service.impl;

import com.supcon.system.base.entity.basic.impl.BasicServiceImpl;
import com.supcon.tptrecommend.convert.question.QuestionConvert;
import com.supcon.tptrecommend.dto.question.QuestionCreateReq;
import com.supcon.tptrecommend.dto.question.QuestionResp;
import com.supcon.tptrecommend.dto.question.QuestionUpdateReq;
import com.supcon.tptrecommend.entity.Question;
import com.supcon.tptrecommend.entity.excel.QuestionImportExcel;
import com.supcon.tptrecommend.mapper.QuestionMapper;
import com.supcon.tptrecommend.service.IQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 问题主表 服务实现类
 * </p>
 *
 * @author luhao
 * @version 1.0.0
 * @date 2025-05-22
 */
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl extends BasicServiceImpl<QuestionMapper, Question> implements IQuestionService {

    private final QuestionMapper questionMapper;

    @Override
    public QuestionResp getObj(Long id) {
        Question question = questionMapper.selectById(id);
        return QuestionConvert.INSTANCE.convert(question);
    }

    @Override
    public Long saveObj(QuestionCreateReq questionCreateReq) {
        Question question = QuestionConvert.INSTANCE.convert(questionCreateReq);
        questionMapper.insert(question);
        return question.getId();
    }

    @Override
    public boolean updateObj(QuestionUpdateReq questionUpdateReq) {
        Question question = QuestionConvert.INSTANCE.convert(questionUpdateReq);
        return questionMapper.updateById(question) > 0;
    }

    @Override
    public boolean removeObjs(List<Long> ids) {
        return questionMapper.deleteBatchIds(ids) > 0;
    }

    @Override
    public void importData(List<QuestionImportExcel> sucData) {
        List<Question> questions = QuestionConvert.INSTANCE.convertFromExcel(sucData);
        saveBatch(questions);

    }
}
