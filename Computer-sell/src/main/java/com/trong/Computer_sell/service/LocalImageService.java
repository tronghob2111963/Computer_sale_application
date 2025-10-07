package com.trong.Computer_sell.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocalImageService {

    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/products/";

    public String saveImage(MultipartFile file) {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Trả về URL mà FE có thể truy cập
            return "/uploads/products/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi lưu ảnh: " + e.getMessage(), e);
        }
    }
    
}
