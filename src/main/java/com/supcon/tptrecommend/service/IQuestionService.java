package com.supcon.tptrecommend.service;

import com.supcon.system.base.entity.basic.IBasicService;
import com.supcon.tptrecommend.dto.question.QuestionCreateReq;
import com.supcon.tptrecommend.dto.question.QuestionResp;
import com.supcon.tptrecommend.dto.question.QuestionUpdateReq;
import com.supcon.tptrecommend.entity.Question;
import com.supcon.tptrecommend.entity.excel.QuestionImportExcel;

import java.util.List;

/**
 * <p>
 * 问题主表 服务类
 * </p>
 *
 * @author luhao
 * @version 1.0.0
 * @date 2025-05-22
 */
public interface IQuestionService extends IBasicService<Question> {

    /**
     * 获取
     *
     * @param id 对象ID
     * @return 对象响应实体
     * @author luhao
     * @date 2025-05-22
     */
    QuestionResp getObj(Long id);

    /**
     * 保存
     *
     * @param questionCreateReq 对象创建实体
     * @return 对象ID
     * @author luhao
     * @date 2025-05-22
     */
    Long saveObj(QuestionCreateReq questionCreateReq);

    /**
     * 更新
     *
     * @param questionUpdateReq 对象更新实体
     * @return 更新是否成功
     * @author luhao
     * @date 2025-05-22
     */
    boolean updateObj(QuestionUpdateReq questionUpdateReq);


    /**
     * 批量删除
     *
     * @param ids 对象ID集合
     * @return 批量删除时是否成功
     * @author luhao
     * @date 2025-05-22
     */
    boolean removeObjs(List<Long> ids);

    void importData(List<QuestionImportExcel> sucData);
}
