package com.supcon.tptrecommend.dto.question;

import com.supcon.tptrecommend.entity.Question;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

/**
 * <p>
 * 问题主表
 * </p>
 *
 * @author luhao
 * @version 1.0.0
 * @date 2025-05-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@ApiModel(value = "问题主表-数据模型", description = "问题主表")
public class QuestionDTO extends Question {

    private static final long serialVersionUID = 1L;


    /**
     * <p>Title:获取entity对象</p>
     * <p>Description:</p>
     * @author luhao
     * @date 2025-05-22
     * @return entity对象
     */
    public Question toQuestion() {
        Question question = new Question();
        BeanUtils.copyProperties(this, question);
        return question;
    }
}