package com.supcon.tptrecommend.dto.fileobject;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
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

    /**
     * 查找或创建子节点
     *
     * @param childName 子项名称
     * @param id        文件id
     * @param type      类型
     * @param path      路径
     * @return {@link FileTreeNode }
     * @author luhao
     * @since 2025/07/04 11:12:16
     */
    public FileTreeNode findOrCreateChild(String childName, Long id, String type,String path) {
        for (FileTreeNode child : this.children) {
            if (child.getName().equals(childName)) {
                return child;
            }
        }
        FileTreeNode newChild = new FileTreeNode(id, childName.substring(childName.indexOf("_") + 1), type,path);
        this.children.add(newChild);
        this.children.sort(Comparator.comparing(FileTreeNode::getType).reversed());
        return newChild;
    }
}