package com.fww.utils;

import com.fww.file.FileInfo;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

//重构！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
public class FileOperation {
    public final static String ROOT_PATH = "root";

    public static ResponseEntity<Resource> preview(Integer pid, String name, String attachmentType) throws IOException {
        Path directoryPath = Paths.get(ROOT_PATH, attachmentType, pid.toString());
        Path filePath = null;
        Resource resource = null;

        if (name.contains(".")) {
            filePath = directoryPath.resolve(name);
            resource = new UrlResource(filePath.toUri());
        } else {
            try (Stream<Path> files = Files.list(directoryPath)) {
                String finalName = name;
                Optional<Path> matchingFile = files.filter(file -> StringUtils.stripFilenameExtension(file.getFileName().toString()).equals(finalName))
                        .findFirst();
                if (matchingFile.isPresent()) {
                    filePath = matchingFile.get();
                    resource = new UrlResource(filePath.toUri());
                    name = filePath.getFileName().toString();
                }
            }
        }

        if (resource == null || !resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }

        String contentType = Files.probeContentType(filePath);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + name + "\"")
                .body(resource);
    }

    public static ResponseEntity<Resource> download(Path directoryPath, String name) {
        try {
            Path filePath = null;
            Resource resource = null;

            if (name.contains(".")) {
                filePath = directoryPath.resolve(name);
                resource = new UrlResource(filePath.toUri());
            } else {
                try (Stream<Path> files = Files.list(directoryPath)) {
                    String finalName = name;
                    Optional<Path> matchingFile = files.filter(file -> StringUtils.stripFilenameExtension(file.getFileName().toString()).equals(finalName))
                            .findFirst();
                    if (matchingFile.isPresent()) {
                        filePath = matchingFile.get();
                        resource = new UrlResource(filePath.toUri());
                        name = filePath.getFileName().toString();
                    }
                }
            }

            if (resource == null || !resource.exists() || !resource.isReadable()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + URLEncoder.encode(name, "UTF-8") + "\"")
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }



    public static FileInfo getFileInfo(String... filePath) throws IOException {
        Path path = Paths.get("", filePath);
        FileInfo fileInfo = new FileInfo();
        fileInfo.setPath(path.toString());
        fileInfo.setExists(Files.exists(path));

        if (fileInfo.isExists()) {
            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
            long sizeInByte = attrs.size();
            final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
            int digitGroups = (int) (Math.log10(sizeInByte) / Math.log10(1024));

            DecimalFormat decimalFormat = new DecimalFormat("#,##0.#");
            String size = decimalFormat.format(sizeInByte / Math.pow(1024, digitGroups)) + " " + units[digitGroups];

            fileInfo.setSize(size);
            fileInfo.setCreationTime(attrs.creationTime().toInstant());
            fileInfo.setLastAccessTime(attrs.lastAccessTime().toInstant());
            fileInfo.setLastModifiedTime(attrs.lastModifiedTime().toInstant());
            fileInfo.setFileName(path.getFileName().toString());
            fileInfo.setParentDirectory(path.getParent().toString());

            if (attrs.isDirectory()) {
                fileInfo.setType("Directory");
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                    fileInfo.setEmptyDirectory(!stream.iterator().hasNext());
                }
            } else if (attrs.isRegularFile()) {
                fileInfo.setType("File");
            } else {
                fileInfo.setType("Other");
            }
        }

        return fileInfo;
    }
    public static void copyFileOrDirectory(String sourcePath, String targetPath) throws IOException {
        Path source = Paths.get(sourcePath);
        Path target = Paths.get(targetPath);

        if (Files.exists(target)) {
            String uniqueFileName = getUniqueFileName(target.getFileName().toString(), target.getParent().toString());
            target = target.resolveSibling(uniqueFileName);
        }

        if (Files.isDirectory(source)) {
            Path finalTarget = target;
            try (Stream<Path> walkPaths = Files.walk(source)){
                walkPaths.forEach(m -> {
                            Path targetPathTemp = finalTarget.resolve(source.relativize(m));
                            try {
                                Files.copy(m, targetPathTemp, StandardCopyOption.REPLACE_EXISTING);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
            }
        } else {
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static void moveFileOrDirectory(String[] oldPath, String[] newPath) throws IOException {
        System.out.println(Arrays.toString(oldPath));
        System.out.println(Arrays.toString(newPath));
        Path oldDirectoryPath = Paths.get("", oldPath);
        Path newDirectoryPath = Paths.get("", newPath);

        if (Files.isRegularFile(oldDirectoryPath)) {
            if (Files.isDirectory(newDirectoryPath)) {
                Path targetPath = newDirectoryPath.resolve(oldDirectoryPath.getFileName());
                Files.move(oldDirectoryPath, targetPath);
            } else if (Files.isRegularFile(newDirectoryPath) || !Files.exists(newDirectoryPath)) {
                if (oldDirectoryPath.getFileName().equals(newDirectoryPath.getFileName())) {
                    String newFileName = getUniqueFileName(oldDirectoryPath.getFileName().toString(), newDirectoryPath.getParent().toString());
                    newDirectoryPath = newDirectoryPath.resolveSibling(newFileName);
                }
                newDirectoryPath = addExtension(oldDirectoryPath, newDirectoryPath);
                Files.move(oldDirectoryPath, newDirectoryPath);
                System.out.println(newDirectoryPath);
            }
        } else if (Files.isDirectory(oldDirectoryPath)) {
            try (Stream<Path> walkPaths = Files.walk(oldDirectoryPath)){
                Path finalNewDirectoryPath = newDirectoryPath;
                walkPaths.forEach(source -> {
                    try {
                        Path target = finalNewDirectoryPath.resolve(oldDirectoryPath.relativize(source));
                        Files.move(source, target);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to move file: " + source, e);
                    }
                });
            }
        }
    }

    private static Path addExtension(Path oldPath, Path newPath) {
        String oldExtension = getFileExtension(oldPath);
        String newExtension = getFileExtension(newPath);

        if(newExtension.isEmpty()) {
            String fileName = newPath.getFileName().toString();
            fileName = fileName + "." + oldExtension;
            return newPath.getParent().resolve(fileName);
        } else {
            return newPath;
        }
    }

    private static String getFileExtension(Path path) {
        String fileName = path.getFileName().toString();
        int index = fileName.lastIndexOf('.');
        if (index == -1) {
            return "";
        }
        return fileName.substring(index + 1);
    }
    public static void saveFile(MultipartFile file, String... path) throws IOException {
        String fileName = Objects.requireNonNull(file.getOriginalFilename());
        fileName = getUniqueFileName(fileName, path);
        Path directoryPath = Paths.get("", path);
        Files.createDirectories(directoryPath);
        Path filePath = directoryPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);
    }

    public static void saveFile(String name, MultipartFile file, String... path) throws IOException {
        name = getUniqueFileName(name, path);
        String oldFileName = file.getOriginalFilename();
        if(getExtension(name).isEmpty()){
            if (oldFileName != null) {
                name = name + getExtension(oldFileName);
            }
        }
        Path directoryPath = Paths.get("", path);
        Files.createDirectories(directoryPath);
        Path filePath = directoryPath.resolve(name);
        Files.copy(file.getInputStream(), filePath);
    }

    public static void deleteFileOrDirectory(boolean force, String... path) throws IOException {
        Path directoryPath = Paths.get("", path);
        System.out.println("delete");
        System.out.println(Arrays.toString(path));

        if (!Files.exists(directoryPath)) {
            return;
        }

        if (Files.isRegularFile(directoryPath)) {
            Files.delete(directoryPath);
        } else if (Files.isDirectory(directoryPath)) {
            try (Stream<Path> walkPaths = Files.walk(directoryPath);
                 Stream<Path> listPaths = Files.list(directoryPath)) {
                if (force) {
                    walkPaths.map(Path::toFile)
                            .sorted((o1, o2) -> -o1.compareTo(o2))
                            .forEach(file -> {
                                try {
                                    Files.delete(file.toPath());
                                } catch (IOException e) {
                                    throw new RuntimeException("文件删除失败: " + file, e);
                                }
                            });
                } else {
                    if (listPaths.findAny().isEmpty()) {
                        Files.delete(directoryPath);
                    }else {
                        throw new IOException("文件夹非空！");
                    }
                }
            }
        }
    }


    private static String getUniqueFileName(String name, String... path) throws IOException {
        Path directoryPath = Paths.get("", path);
        String baseName = getBaseName(name);
        String extension = getExtension(name);
        Path filePath = directoryPath.resolve(name);
        int count = 1;

        while (Files.exists(filePath)) {
            String newName = baseName + "(" + count + ")" + extension;
            filePath = directoryPath.resolve(newName);
            count++;
        }

        return filePath.getFileName().toString();
    }

    private static String getBaseName(String name) {
        int index = name.lastIndexOf(".");
        if (index == -1) {
            return name;
        } else {
            return name.substring(0, index);
        }
    }

    private static String getExtension(String name) {
        int index = name.lastIndexOf(".");
        if (index == -1) {
            return "";
        } else {
            return name.substring(index);
        }
    }

    //    public static ResponseEntity<Resource> preview(Integer pid, String name, AttachmentType attachmentType) throws IOException {
//        Path filePath = Paths.get(ROOT_PATH, attachmentType.getPath(), pid.toString(), name);
//        Resource resource = new UrlResource(filePath.toUri());
//
//        if (!resource.exists() || !resource.isReadable()) {
//            return ResponseEntity.notFound().build();
//        }
//
//        String contentType = Files.probeContentType(filePath);
//        if (contentType == null) {
//            contentType = "application/octet-stream";
//        }
//
//        return ResponseEntity.ok()
//                .contentType(MediaType.parseMediaType(contentType))
//                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + name + "\"")
//                .body(resource);
//    }

//        public static ResponseEntity<Resource> download(Path path) {
//        try {
//            Resource resource = new UrlResource(path.toUri());
//
//            if (resource.exists() && resource.isReadable()) {
//                String contentType = Files.probeContentType(path);
//
//                if (contentType == null) {
//                    contentType = "application/octet-stream";
//                }
//
//                return ResponseEntity.ok()
//                        .contentType(MediaType.parseMediaType(contentType))
//                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + URLEncoder.encode(Objects.requireNonNull(resource.getFilename()),"UTF-8") + "\"")
//                        .body(resource);
//            } else {
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//            }
//        } catch (IOException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
}
