package com.codemania.templateupload.controller;

import com.codemania.templateupload.dto.FileUploadResult;
import com.codemania.templateupload.dto.UploadResponse;
import com.codemania.templateupload.service.FileUploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
public class FileUploadController {

    private final FileUploadService fileUploadService;

    public FileUploadController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    @GetMapping("/")
    public String showUploadForm() {
        return "upload";
    }

    /**
     * AJAX endpoint for file upload.
     * Returns JSON response with upload results.
     */
    @PostMapping("/api/upload")
    @ResponseBody
    public ResponseEntity<UploadResponse> handleFileUpload(
            @RequestParam("files") MultipartFile[] files
    ) {
        if (files == null || files.length == 0) {
            return ResponseEntity.badRequest()
                    .body(UploadResponse.error("No files provided"));
        }

        // Filter out empty files
        List<MultipartFile> validFiles = java.util.Arrays.stream(files)
                .filter(f -> !f.isEmpty())
                .toList();

        if (validFiles.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(UploadResponse.error("No valid files to upload"));
        }

        List<FileUploadResult> results = fileUploadService.saveFiles(
                validFiles.toArray(new MultipartFile[0])
        );

        return ResponseEntity.ok(UploadResponse.success(results));
    }
}
