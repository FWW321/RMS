package com.fww.file;

import lombok.Data;

import java.time.Instant;

@Data
public class FileInfo {
    private String path;
    private String type;
    private String size;
    private Instant creationTime;
    private Instant lastAccessTime;
    private Instant lastModifiedTime;
    private boolean exists;
    private String fileName;
    private String parentDirectory;
    private boolean emptyDirectory;
}
