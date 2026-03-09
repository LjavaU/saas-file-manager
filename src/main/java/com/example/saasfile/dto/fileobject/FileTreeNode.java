package com.example.saasfile.dto.fileobject;

import com.example.saasfile.common.enums.FileKind;
import com.example.saasfile.entity.FileObject;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Data
@NoArgsConstructor
public class FileTreeNode {

    @ApiModelProperty(value = "")
    private Long id;

    @ApiModelProperty(value = "")
    private String name;

    @ApiModelProperty(value = "")
    private String type;

    @ApiModelProperty(value = "")
    private Long size;

    @ApiModelProperty(value = "")
    private String path;

    @ApiModelProperty(value = "")
    private Integer knowledgeParseState;

    private List<FileTreeNode> children = new ArrayList<>();

    public FileTreeNode(Long id, String name, String type, String path, Long size, Integer knowledgeParseState) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.path = path;
        this.size = size;
        this.knowledgeParseState = knowledgeParseState;
    }

    
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