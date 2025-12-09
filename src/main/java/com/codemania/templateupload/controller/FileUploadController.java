package com.codemania.templateupload.controller;

import com.codemania.templateupload.dto.FileUploadResult;
import com.codemania.templateupload.service.FileUploadService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class FileUploadController {

    private final FileUploadService fileUploadService;

    public FileUploadController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    @GetMapping("/")
    public String showUploadForm(Model model) {
        return "upload";
    }

    @PostMapping("/upload")
    public String handleFileUpload(
            @RequestParam("files") MultipartFile[] files,
            RedirectAttributes redirectAttributes
    ) {
        if (files == null || files.length == 0 || (files.length == 1 && files[0].isEmpty())) {
            redirectAttributes.addFlashAttribute("error", "Please select at least one file to upload");
            return "redirect:/";
        }

        List<FileUploadResult> results = fileUploadService.saveFiles(files);

        long successCount = results.stream().filter(FileUploadResult::success).count();
        long failureCount = results.size() - successCount;

        redirectAttributes.addFlashAttribute("results", results);
        redirectAttributes.addFlashAttribute("successCount", successCount);
        redirectAttributes.addFlashAttribute("failureCount", failureCount);

        return "redirect:/";
    }
}
