package com.codemania.templateupload.dto;

import java.util.List;

/**
 * Wrapper response for AJAX file upload operations.
 */
public record UploadResponse(
        boolean success,
        String message,
        int totalFiles,
        int successCount,
        int failureCount,
        List<FileUploadResult> results
) {
    public static UploadResponse success(List<FileUploadResult> results) {
        long successCount = results.stream().filter(FileUploadResult::success).count();
        long failureCount = results.size() - successCount;

        return new UploadResponse(
                true,
                successCount + " file(s) uploaded successfully",
                results.size(),
                (int) successCount,
                (int) failureCount,
                results
        );
    }

    public static UploadResponse error(String message) {
        return new UploadResponse(false, message, 0, 0, 0, List.of());
    }
}
