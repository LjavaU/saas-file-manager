package com.supcon.tptrecommend.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.supcon.system.base.entity.basic.IBaseMapper;
import com.supcon.tptrecommend.dto.fileobject.FileObjectResp;
import com.supcon.tptrecommend.dto.fileobject.FileStatisticsResp;
import com.supcon.tptrecommend.entity.FileObject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * MinIO 文件元数据表 Mapper 接口
 * </p>
 *
 * @author luhao
 * @version 1.0.0
 * @date 2025-05-22
 */
@Mapper
public interface FileObjectMapper extends IBaseMapper<FileObject> {

    /**
     * 获取正在知识库解析中的状态文件
     *
     * @return {@link List }<{@link FileObjectResp }>
     * @author luhao
     * @since 2025/07/30 10:09:34
     */
    @InterceptorIgnore(tenantLine = "true")
    @Select("select id,user_id,tenant_id,bucket_name,object_name from file_object where knowledge_parse_state = #{knowledgeParseState}  ")
    List<FileObjectResp> getKnowledgeParsing(Integer knowledgeParseState);

    /**
     * 更新知识库解析状态
     *
     * @param fileObject 文件对象
     * @author luhao
     * @since 2025/07/30 13:27:59
     */
    @InterceptorIgnore(tenantLine = "true")
    @Select("UPDATE file_object \n" +
        "SET knowledge_parse_state = #{knowledgeParseState}, \n" +
        "update_time = #{updateTime},\n" +
        "file_status = #{fileStatus}" +
        "  where id = #{id}  \n" +
        "  AND tenant_id = #{tenantId} ;")
    void updateKnowledgeParseState(FileObject fileObject);


    /**
     * 统计文件总数和总大小
     *
     * @return {@link FileStatisticsResp }
     * @author luhao
     * @since 2025/09/10 14:45:53
     *
     */
    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT COUNT(*) AS totalFiles, \n" +
        "       SUM(file_size) AS totalSize\n" +
        "FROM file_object where original_name is not null;")
    FileStatisticsResp getFileStatistics();
}
