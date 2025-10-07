package com.trong.Computer_sell.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ImageUploadService {

    private final Cloudinary cloudinary;

    public String uploadImage(String file) {
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap("folder", "products"));
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi upload ảnh", e);
        }
    }
}
