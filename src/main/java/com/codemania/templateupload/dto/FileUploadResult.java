package com.codemania.templateupload.dto;

/**
 * Represents the result of a single file upload operation.
 */
public record FileUploadResult(
        String filename,
        String savedPath,
        FileType fileType,
        boolean success,
        String message
) {
    public enum FileType {
        IMAGE, CSS, HTML, UNKNOWN
    }

    public static FileUploadResult success(String filename, String savedPath, FileType fileType) {
        return new FileUploadResult(filename, savedPath, fileType, true, "Uploaded successfully");
    }

    public static FileUploadResult failure(String filename, String message) {
        return new FileUploadResult(filename, null, FileType.UNKNOWN, false, message);
    }
}
