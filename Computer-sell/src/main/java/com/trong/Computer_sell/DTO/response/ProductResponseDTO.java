package com.trong.Computer_sell.DTO.response;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class ProductResponseDTO {
    private String name;
    private String description;
    private double price;
    private int stock;
    private String brandName;
    private String categoryName;
    private List<MultipartFile> image;
}
