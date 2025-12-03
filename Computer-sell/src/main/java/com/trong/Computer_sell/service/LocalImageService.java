package com.trong.Computer_sell.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocalImageService {

    private static final String BASE_UPLOAD_DIR = "src/main/resources/static/uploads/";

    public String saveImage(MultipartFile file) {
        return uploadImage(file, "products");
    }

    /**
     * Upload ảnh vào thư mục tùy chỉnh
     * @param file File ảnh cần upload
     * @param folder Tên thư mục con (vd: "vietqr-proofs", "products")
     * @return URL của ảnh đã upload
     */
    public String uploadImage(MultipartFile file, String folder) {
        try {
            // Sử dụng đường dẫn tuyệt đối
            Path basePath = Paths.get(BASE_UPLOAD_DIR).toAbsolutePath();
            Path uploadPath = basePath.resolve(folder);
            
            log.info("Upload path: {}", uploadPath);
            
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Created directory: {}", uploadPath);
            }

            // Tạo tên file unique - loại bỏ ký tự đặc biệt
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            }
            // Chỉ giữ extension phổ biến
            if (!extension.matches("\\.(jpg|jpeg|png|gif|webp)")) {
                extension = ".jpg";
            }
            String fileName = UUID.randomUUID().toString() + extension;
            Path filePath = uploadPath.resolve(fileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            log.info("Saved file to: {}", filePath);

            // Trả về URL mà FE có thể truy cập
            return "/uploads/" + folder + "/" + fileName;
        } catch (IOException e) {
            log.error("Error uploading image to folder {}: {}", folder, e.getMessage(), e);
            throw new RuntimeException("Lỗi khi lưu ảnh: " + e.getMessage(), e);
        }
    }
    
}
