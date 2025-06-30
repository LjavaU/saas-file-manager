package com.supcon.tptrecommend.service.impl;

import com.alibaba.excel.EasyExcel;
import com.google.common.collect.Lists;
import com.supcon.framework.tenant.core.getter.TenantContext;
import com.supcon.system.base.entity.basic.impl.BasicServiceImpl;
import com.supcon.systemmanagerapi.dto.LoginInfoUserDTO;
import com.supcon.tptrecommend.common.Constants;
import com.supcon.tptrecommend.common.utils.LoginUserUtils;
import com.supcon.tptrecommend.convert.question.QuestionConvert;
import com.supcon.tptrecommend.dto.question.QuestionCreateReq;
import com.supcon.tptrecommend.dto.question.QuestionResp;
import com.supcon.tptrecommend.dto.question.QuestionUpdateReq;
import com.supcon.tptrecommend.entity.Question;
import com.supcon.tptrecommend.entity.excel.QuestionImportExcel;
import com.supcon.tptrecommend.integration.excel.DropdownSheetWriteHandler;
import com.supcon.tptrecommend.mapper.QuestionMapper;
import com.supcon.tptrecommend.service.IQuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

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
@Slf4j
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


    /**
     * 用户问题导入
     *
     * @param sucData SUC 数据
     * @author luhao
     * @date 2025/05/27 15:33:04
     */
    @Override
    public void userQuesImport(List<QuestionImportExcel> sucData) {
        List<Question> questions = QuestionConvert.INSTANCE.convertFromExcel(sucData);
        LoginInfoUserDTO loginUserInfo = LoginUserUtils.getLoginUserInfo();
        questions.forEach(question -> {
            question.setUserId(loginUserInfo.getId() == null ? 0 : loginUserInfo.getId());
            question.setTenantId(TenantContext.getCurrentTenant());
        });
        // TODO: 批量保存数据不会自动设置租户id
        saveBatch(questions);
    }

    @Override
    public void nonUserQuesImport(List<QuestionImportExcel> sucData) {
        // 过滤出共性问题
        List<QuestionImportExcel> commonQuestions = sucData.stream()
            .filter(questionImportExcel -> questionImportExcel.getIsCommon() == 1)
            .collect(Collectors.toList());
        List<Question> addCommonQuestions = QuestionConvert.INSTANCE.convertFromExcel(commonQuestions);
        addCommonQuestions.forEach(question -> {
            question.setTenantId(Constants.DEFAULT_TENANT_ID);
            question.setIndustry(null);
            question.setPost(null);
            question.setDevice(null);
        });
        //  出非共性问题
        sucData.removeAll(commonQuestions);
        List<Question> addNonCommonQuestions = QuestionConvert.INSTANCE.convertFromExcel(sucData);
        addNonCommonQuestions.forEach(ques -> {
            ques.setTenantId(TenantContext.getCurrentTenant());
        });
        saveBatch(Lists.newArrayList(addCommonQuestions, addNonCommonQuestions).stream()
            .flatMap(Collection::stream)
            .collect(Collectors.toList()));

    }

    @Override
    public void templateDownload(HttpServletResponse response) {
        try {
            // 1. 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            // 防止中文乱码，对文件名进行URL编码
            String fileName = URLEncoder.encode("问题导入模板.xlsx", StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=UTF-8''" + fileName);

            // 2. 准备下拉框数据
            // 准备下拉框数据
            Map<Integer, String[]> dropdownData = new HashMap<>();
            dropdownData.put(4, new String[]{"是", "否"});

            // 3. 创建自定义的Handler实例
            // 下拉列表从数据区的第1行开始（Excel中的第2行，因为第1行是表头），索引为1
            // 应用到数据区的第1000行（Excel中的第1001行），索引为1000
            int firstDataRowIndex = 1; // 表头占了第0行，数据从第1行开始
            int lastDataRowIndex = 100; // 应用1000行数据区域的下拉框
            DropdownSheetWriteHandler dropdownSheetWriteHandler =
                new DropdownSheetWriteHandler(dropdownData, firstDataRowIndex, lastDataRowIndex);


            // 5. 使用EasyExcel写入数据到response的输出流
            EasyExcel.write(response.getOutputStream(), QuestionImportExcel.class)
                .registerWriteHandler(dropdownSheetWriteHandler) // 注册自定义Handler
                .sheet("用户数据模板") // Sheet名称
                .excludeColumnFieldNames(Collections.singletonList("errorMsg"))
                .doWrite(Collections.emptyList()); // 传入空List或少量示例数据
            log.info("Excel模板导出成功: {}", fileName);

        } catch (IOException e) {
            log.error("导出Excel模板失败", e);
            // 重置response，处理异常，例如返回错误信息给前端
            // 注意：如果部分响应头已发送，这里可能无法完全按预期工作
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            try {
                response.getWriter().println("{\"success\": false, \"message\": \"导出文件失败: " + e.getMessage() + "\"}");
            } catch (IOException ioException) {
                log.error("写入错误响应失败", ioException);
            }
        }
    }
}
