package com.trong.Computer_sell.controller;

import com.trong.Computer_sell.DTO.response.common.ResponseData;
import com.trong.Computer_sell.DTO.response.common.ResponseError;
import com.trong.Computer_sell.service.ProductTypeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/product-types")
@Tag(name = "Product Type", description = "Product Type API")
@RequiredArgsConstructor
@Slf4j
public class ProductTypeController {
    private final ProductTypeService productTypeService;

    @GetMapping("/list")
    public ResponseData<Object> listProductType() {
        log.info("List product type");
        try {
            log.info("List product type");
            return new ResponseData<>(HttpStatus.ACCEPTED.value(), "Product type found successfully", productTypeService.getAllProductType());
        } catch (Exception e) {
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }
}
