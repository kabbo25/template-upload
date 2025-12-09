package com.codemania.templateupload.service;

import com.codemania.templateupload.dto.FileUploadResult;
import com.codemania.templateupload.dto.FileUploadResult.FileType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class FileUploadService {

    @Value("${app.upload.base-path}")
    private String basePath;

    private static final Map<String, FileType> EXTENSION_MAP = Map.ofEntries(
            // Images
            Map.entry("jpg", FileType.IMAGE),
            Map.entry("jpeg", FileType.IMAGE),
            Map.entry("png", FileType.IMAGE),
            Map.entry("gif", FileType.IMAGE),
            Map.entry("webp", FileType.IMAGE),
            Map.entry("svg", FileType.IMAGE),
            Map.entry("ico", FileType.IMAGE),
            Map.entry("bmp", FileType.IMAGE),
            // CSS
            Map.entry("css", FileType.CSS),
            // HTML
            Map.entry("html", FileType.HTML),
            Map.entry("htm", FileType.HTML)
    );

    private static final Map<FileType, String> FOLDER_MAP = Map.of(
            FileType.IMAGE, "images",
            FileType.CSS, "css",
            FileType.HTML, "html"
    );

    private static final Set<String> ALLOWED_EXTENSIONS = EXTENSION_MAP.keySet();

    public List<FileUploadResult> saveFiles(MultipartFile[] files) {
        List<FileUploadResult> results = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }
            results.add(saveFile(file));
        }

        return results;
    }

    public FileUploadResult saveFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null || originalFilename.isBlank()) {
            return FileUploadResult.failure("unknown", "Invalid filename");
        }

        String sanitizedFilename = sanitizeFilename(originalFilename);
        String extension = getFileExtension(sanitizedFilename).toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            return FileUploadResult.failure(
                    sanitizedFilename,
                    "File type not allowed. Allowed: " + String.join(", ", ALLOWED_EXTENSIONS)
            );
        }

        FileType fileType = EXTENSION_MAP.get(extension);
        String targetFolder = FOLDER_MAP.get(fileType);

        try {
            Path targetDirectory = Paths.get(basePath, targetFolder);
            Files.createDirectories(targetDirectory);

            Path targetPath = targetDirectory.resolve(sanitizedFilename);

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            String savedPath = targetFolder + "/" + sanitizedFilename;
            return FileUploadResult.success(sanitizedFilename, savedPath, fileType);

        } catch (IOException e) {
            return FileUploadResult.failure(sanitizedFilename, "Failed to save file: " + e.getMessage());
        }
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }

    private String sanitizeFilename(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
