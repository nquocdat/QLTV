package com.example.be_qltv.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileUploadService {

    @Value("${file.upload.dir:uploads/books}")
    private String uploadDir;

    /**
     * Upload file và trả về relative path
     */
    public String uploadFile(MultipartFile file) throws IOException {
        // Validate file
        if (file.isEmpty()) {
            throw new IOException("Cannot upload empty file");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IOException("Only image files are allowed");
        }

        // Validate file size (max 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IOException("File size must be less than 10MB");
        }

        // Create upload directory if not exists
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = UUID.randomUUID().toString() + extension;

        // Save file
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Return relative path (will be served by static resource handler)
        return "/uploads/books/" + filename;
    }

    /**
     * Delete file
     */
    public void deleteFile(String filePath) {
        try {
            if (filePath != null && filePath.startsWith("/uploads/")) {
                Path path = Paths.get(uploadDir, 
                    filePath.substring(filePath.lastIndexOf("/") + 1));
                Files.deleteIfExists(path);
            }
        } catch (IOException e) {
            // Log error but don't throw exception
            System.err.println("Error deleting file: " + e.getMessage());
        }
    }
}
