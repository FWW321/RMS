package com.fww.file;

import com.fww.enumeration.FileType;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DirectoryNode {
    private String name;
    private String parentName;
    private String path;
    private FileType fileType;
    private List<DirectoryNode> children;

    public DirectoryNode(String name, String parentName, String path, FileType fileType) {
        this.name = name;
        this.parentName = parentName;
        this.path = path;
        this.fileType = fileType;
        this.children = new ArrayList<>();
    }

    public void addChild(DirectoryNode child) {
        this.children.add(child);
    }

    @Override
    public String toString() {
        return "DirectoryNode{" +
                "name='" + name + '\'' +
                ", parentName='" + parentName + '\'' +
                ", path='" + path + '\'' +
                ", fileType=" + fileType +
                ", children=" + children +
                '}';
    }
}