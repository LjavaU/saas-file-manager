package com.supcon.tptrecommend.manager;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.supcon.systemcommon.entity.IDList;
import com.supcon.systemcommon.entity.SupRequestBody;
import com.supcon.tptrecommend.dto.fileobject.FileObjectResp;
import com.supcon.tptrecommend.dto.fileobject.SingleFileQueryReq;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Map;

public interface FileManager {
    /**
     * 上传
     *
     * @param multipartFile Multipart 文件
     * @param attributes
     * @return {@link Boolean }
     * @author luhao
     * @date 2025/05/22 15:36:07
     */
    FileObjectResp upload(MultipartFile multipartFile, String attributes);

    /**
     * 删除
     *
     * @param id 主键
     * @return {@link Boolean }
     * @author luhao
     * @date 2025/05/22 15:36:10
     */
    Boolean delete(Long id);

    /**
     * 文件列表
     *
     * @param body 请求体
     * @return {@link IPage }<{@link FileObjectResp }>
     * @throws Exception 例外
     * @author luhao
     * @date 2025/05/22 15:36:16
     */
    IPage<FileObjectResp>  selectPage(@Valid SupRequestBody<Map<String, String>> body) throws Exception;

    /**
     * 获取单个文件流
     *
     * @param req      请求体
     * @param response 响应
     * @throws IOException io异常
     * @author luhao
     * @date 2025/05/29 16:49:37
     */
    void getOne(SingleFileQueryReq req, HttpServletResponse response) throws IOException;

    String convertToMarkdown(MultipartFile file) throws Exception;

    FileObjectResp detail(Long fileId);


    /**
     * 批量删除
     *
     * @param data 文件id
     * @return {@link Boolean }
     * @author luhao
     * @date 2025/06/04 19:44:28
     */
    Boolean batchDelete(IDList<Long> data);
}
