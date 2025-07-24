package com.supcon.tptrecommend.dto.fileobject;

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

    private List<FileTreeNode> children = new ArrayList<>(); // 子节点列表

    public FileTreeNode(Long id, String name, String type,String path,Long size) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.path = path;
        this.size = size;
    }

    /**
     * 查找或创建子节点
     *
     * @param childName 子项名称
     * @param id        文件id
     * @param type      类型
     * @param path      路径
     * @param size      文件大小
     * @return {@link FileTreeNode }
     * @author luhao
     * @since 2025/07/04 11:12:16
     */
    public FileTreeNode findOrCreateChild(String childName, Long id, String type,String path,Long size) {
        for (FileTreeNode child : this.children) {
            if (child.getName().equals(childName)) {
                return child;
            }
        }
        FileTreeNode newChild = new FileTreeNode(id, childName.substring(childName.indexOf("_") + 1), type,path,size);
        this.children.add(newChild);
        this.children.sort(Comparator.comparing(FileTreeNode::getType).reversed());
        return newChild;
    }
}