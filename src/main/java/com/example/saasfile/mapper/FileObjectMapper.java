package com.example.saasfile.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.example.saasfile.support.mybatis.IBaseMapper;
import com.example.saasfile.dto.fileobject.FileAttributesUpdatedCondition;
import com.example.saasfile.dto.fileobject.FileObjectResp;
import com.example.saasfile.dto.fileobject.FileStatisticsResp;
import com.example.saasfile.entity.FileObject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;


@Mapper
public interface FileObjectMapper extends IBaseMapper<FileObject> {

    
    @InterceptorIgnore(tenantLine = "true")
    @Select("select id,user_id,tenant_id,bucket_name,object_name from file_object where knowledge_parse_state = #{knowledgeParseState}  ")
    List<FileObjectResp> getKnowledgeParsing(Integer knowledgeParseState);

    
    @InterceptorIgnore(tenantLine = "true")
    @Select("UPDATE file_object \n" +
        "SET knowledge_parse_state = #{knowledgeParseState}, \n" +
        "update_time = #{updateTime},\n" +
        "file_status = #{fileStatus}" +
        "  where id = #{id}  \n" +
        "  AND tenant_id = #{tenantId} ;")
    void updateKnowledgeParseState(FileObject fileObject);


    
    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT COUNT(*) AS totalFiles, \n" +
        "       SUM(file_size) AS totalSize\n" +
        "FROM file_object where original_name is not null;")
    FileStatisticsResp getFileStatistics();

    
    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT id,sub_category,third_level_category,ability  \n" +
        "FROM file_object where object_name =#{objectName} ;")
    FileObject getByObjectName(String objectName);

    
    @InterceptorIgnore(tenantLine = "true")
    void updateFileAttributes(@Param("req") FileAttributesUpdatedCondition updatedCondition);
}
