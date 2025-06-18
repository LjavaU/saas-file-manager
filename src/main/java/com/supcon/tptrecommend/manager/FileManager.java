package com.supcon.tptrecommend.manager;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.supcon.systemcommon.entity.IDList;
import com.supcon.systemcommon.entity.SupRequestBody;
import com.supcon.tptrecommend.dto.fileobject.CreateFolderReq;
import com.supcon.tptrecommend.dto.fileobject.FileNodeResp;
import com.supcon.tptrecommend.dto.fileobject.FileObjectResp;
import com.supcon.tptrecommend.dto.fileobject.SingleFileQueryReq;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface FileManager {
    /**
     * 上传
     *
     * @param multipartFile Multipart 文件
     * @param attributes
     * @param path  文件路径
     * @return {@link Boolean }
     * @author luhao
     * @since 2025/06/12 16:26:40
     */
    FileObjectResp upload(MultipartFile multipartFile, String attributes,String path);

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

    /**
     * 创建文件夹
     *
     * @param data 数据
     * @return boolean
     * @author luhao
     * @since 2025/06/11 16:40:41
     */
    boolean createFolder(CreateFolderReq data);


    /**
     * 获取文件夹层级结构
     *
     * @param path 路径
     * @return {@link List }<{@link FileNodeResp }>
     * @author luhao
     * @since 2025/06/12 14:11:37
     */
    List<FileNodeResp> listFiles(String path);
}
