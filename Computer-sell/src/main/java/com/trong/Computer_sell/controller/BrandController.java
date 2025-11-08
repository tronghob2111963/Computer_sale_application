package com.trong.Computer_sell.controller;


import com.trong.Computer_sell.DTO.request.product.BrandRequestDTO;
import com.trong.Computer_sell.DTO.response.common.ResponseData;
import com.trong.Computer_sell.DTO.response.common.ResponseError;
import com.trong.Computer_sell.service.BrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j(topic = "BRAND_CONTROLLER")
@RestController
@RequestMapping("/brand")
@Tag(name = "Brand Management")
@RequiredArgsConstructor
@Validated
public class BrandController {
    private final BrandService brandService;

    @Operation(summary = "create brand", description = "Create brand")
    @PostMapping("/save")
//    @PreAuthorize("hasAnyAuthority('SysAdmin','Admin', 'Staff')")
    public ResponseData<Object> saveBrand(@RequestBody BrandRequestDTO brand){
        log.info("Save brand with brand name", brand.getName());
        try {
            log.info("Save brand with brand name", brand.getName());
            brandService.saveBrand(brand);
            return new ResponseData<>(HttpStatus.ACCEPTED.value(), "Brand saved successfully");
        } catch (Exception e) {
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }
    @Operation(summary = "get brand by id", description = "Get brand by id")
    @GetMapping("/get/{id}")
    public ResponseData<Object> getBrandById(@PathVariable UUID id){
        log.info("Get brand by id");
        try {
            log.info("Get brand by id");
            return new ResponseData<>(HttpStatus.OK.value(), "Brand found successfully", brandService.getBrandById(id));
        } catch (Exception e) {
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Operation(summary = "List brand", description = "List of branch")
    @GetMapping("/list-branch")
    public ResponseData<Object> listBrand(
            @RequestParam(required = false) String keyword ,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy){
        log.info("List brand");
        try {
            log.info("List brand");
            return new ResponseData<>(HttpStatus.OK.value(), "Brand found successfully", brandService.getAllBrands(keyword, page, size, sortBy));
        } catch (Exception e) {
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }
}
