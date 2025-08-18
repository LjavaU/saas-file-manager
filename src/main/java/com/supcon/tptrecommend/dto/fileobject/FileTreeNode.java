package com.supcon.tptrecommend.dto.fileobject;

import com.supcon.tptrecommend.common.enums.FileKind;
import com.supcon.tptrecommend.entity.FileObject;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Data
@NoArgsConstructor
public class FileTreeNode {

    @ApiModelProperty(value = "节点标识")
    private Long id;

    @ApiModelProperty(value = "文件或文件夹名")
    private String name;

    @ApiModelProperty(value = "节点类型: \"folder\" 或 \"file\"")
    private String type;

    @ApiModelProperty(value = "文件大小")
    private Long size;

    @ApiModelProperty(value = "文件路径")
    private String path;

    @ApiModelProperty(value = "知识库解析状态【0-正在上传/解析中，1-embedding失败，2-向量库插入失败，3-成功】")
    private Integer knowledgeParseState;

    private List<FileTreeNode> children = new ArrayList<>(); // 子节点列表

    public FileTreeNode(Long id, String name, String type, String path, Long size, Integer knowledgeParseState) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.path = path;
        this.size = size;
        this.knowledgeParseState = knowledgeParseState;
    }

    /**
     * 查找或创建子节点
     *
     * @param childName  子项名称
     * @param type       类型
     * @param fileObject 文件对象
     * @return {@link FileTreeNode }
     * @author luhao
     * @since 2025/07/04 11:12:16
     *
     *
     */
    public FileTreeNode findOrCreateChild(String childName, String type, FileObject fileObject) {
        for (FileTreeNode child : this.children) {
            if (child.getName().equals(childName)) {
                return child;
            }
        }
        String objectName = fileObject.getObjectName();
        Long id = fileObject.getId();
        Long fileSize = fileObject.getFileSize();
        Integer knowledgeParseState = fileObject.getKnowledgeParseState();
        FileTreeNode newChild = null;
        if (FileKind.FILE.getValue().equals(type)) {
            newChild = new FileTreeNode(id, fileObject.getOriginalName(), type, objectName, fileSize, knowledgeParseState);

        } else if (FileKind.FOLDER.getValue().equals(type)) {
            objectName = objectName.substring(0, objectName.lastIndexOf("/") + 1);
            newChild = new FileTreeNode(id, childName, type, objectName, fileSize, knowledgeParseState);
        }
        this.children.add(newChild);
        this.children.sort(Comparator.comparing(FileTreeNode::getType).reversed());
        return newChild;
    }
}