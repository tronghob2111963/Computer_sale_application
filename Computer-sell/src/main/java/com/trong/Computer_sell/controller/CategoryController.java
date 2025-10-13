package com.trong.Computer_sell.controller;


import com.trong.Computer_sell.DTO.response.ResponseData;
import com.trong.Computer_sell.DTO.response.ResponseError;
import com.trong.Computer_sell.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j(topic = "CATEGORY_CONTROLLER")
@RestController
@RequestMapping("/category")
@Tag(name = "Category Management")
@RequiredArgsConstructor
@Validated
public class CategoryController {
    private final CategoryService categoryService;

    @Operation(summary = "Get all categories", description = "Get all categories")
    @GetMapping
    public ResponseData<Object> getAllCategories(
            @RequestParam(required = false) String keyword ,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy) {
        log.info("Get all categories");
        try{
            log.info("Get all categories");
            return new ResponseData<>(HttpStatus.ACCEPTED.value(), "Categories found successfully", categoryService.getAllCategories(keyword, page, size, sortBy));
        } catch (Exception e) {
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Operation(summary = "Get category by id", description = "Get category by id")
    @GetMapping("/get/{id}")
    public ResponseData<Object> getCategoryById(@PathVariable UUID id){
        log.info("Get category by id");
        try {
            log.info("Get category by id");
            return new ResponseData<>(HttpStatus.OK.value(), "Category found successfully", categoryService.getCategoryById(id));
        } catch (Exception e) {
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }
}
