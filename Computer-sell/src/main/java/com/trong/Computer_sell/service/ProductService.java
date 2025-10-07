package com.trong.Computer_sell.service;

import com.trong.Computer_sell.DTO.request.ProductRequestDTO;
import com.trong.Computer_sell.DTO.response.ProductResponseDTO;
import com.trong.Computer_sell.model.ProductEntity;

import java.util.UUID;

public interface ProductService {
    UUID createProduct(ProductRequestDTO productRequestDTO);

}
