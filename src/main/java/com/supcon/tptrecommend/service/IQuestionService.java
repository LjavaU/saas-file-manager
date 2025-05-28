package com.supcon.tptrecommend.service;

import com.supcon.system.base.entity.basic.IBasicService;
import com.supcon.tptrecommend.dto.question.QuestionCreateReq;
import com.supcon.tptrecommend.dto.question.QuestionResp;
import com.supcon.tptrecommend.dto.question.QuestionUpdateReq;
import com.supcon.tptrecommend.entity.Question;
import com.supcon.tptrecommend.entity.excel.QuestionImportExcel;

import javax.servlet.http.HttpServletResponse;
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



    /**
     * 用户问题导入
     *
     * @param sucData SUC 数据
     * @author luhao
     * @date 2025/05/27 15:32:53
     */
    void userQuesImport(List<QuestionImportExcel> sucData);

    /**
     * 非用户 问题 导入
     *
     * @param sucData SUC 数据
     * @author luhao
     * @date 2025/05/27 16:24:48
     */
    void nonUserQuesImport(List<QuestionImportExcel> sucData);

    /**
     * 模板下载
     *
     * @param response 响应
     * @author luhao
     * @date 2025/05/27 16:52:51
     */
    void templateDownload(HttpServletResponse response);
}
