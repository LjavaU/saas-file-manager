package com.supcon.tptrecommend.dto.fileobject;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class FileTreeNode {
    /**
     * 节点的唯一标识
     */
    private Long id;
    /**
     * 文件名或文件夹名
     */
    private String name;
    /**
     * 节点类型: "folder" 或 "file"
     */
    private String type;


    /**
     * 文件路径
     */
    private String path;

    private List<FileTreeNode> children = new ArrayList<>(); // 子节点列表

    public FileTreeNode(Long id, String name, String type,String path) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.path = path;
    }

    // 辅助方法：查找或创建子节点
    public FileTreeNode findOrCreateChild(String childName, Long id, String type,String path) {
        for (FileTreeNode child : this.children) {
            if (child.getName().equals(childName)) {
                return child;
            }
        }
        FileTreeNode newChild = new FileTreeNode(id, childName, type,path);
        this.children.add(newChild);
        return newChild;
    }
}