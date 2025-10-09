package com.trong.Computer_sell.controller;


import com.trong.Computer_sell.DTO.request.ProductRequestDTO;
import com.trong.Computer_sell.DTO.request.ProductUpdateRequestDTO;
import com.trong.Computer_sell.DTO.response.ResponseData;
import com.trong.Computer_sell.DTO.response.ResponseError;
import com.trong.Computer_sell.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j(topic = "PRODUCT_CONTROLLER")
@RestController
@RequestMapping("/product")
@Tag(name = "Product Management")
@RequiredArgsConstructor
@Validated
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Create product", description = "Create product")
    @RequestBody(
            required = true,
            description = "Product request",
            content = @Content(
                    mediaType = "multipart/form-data",
                    schema = @Schema(implementation = ProductRequestDTO.class)
            )
    )
    @PostMapping("/create")
    public ResponseData<Object> createProduct(@ModelAttribute ProductRequestDTO dto){
        log.info("Create product with product name", dto.getName());
        try{
            log.info("Create product with product name", dto.getName());

            return new ResponseData<>(HttpStatus.ACCEPTED.value(), "Product created successfully", productService.createProduct(dto));
        }catch (Exception e){
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Operation(summary = "Product List", description = "Product List")
    @GetMapping("/list")
    public ResponseData<Object> listProduct(
            @RequestParam(required = false) String keyword ,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy
    ){
        log.info("List product");
        try {
            log.info("List product");
            return new ResponseData<>(HttpStatus.ACCEPTED.value(), "Product found successfully", productService.getAllProducts(keyword, page, size, sortBy));
        } catch (Exception e) {
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Operation(summary = "Update product", description = "Update product")
    @RequestBody(
            required = true,
            description = "Product request",
            content = @Content(
                    mediaType = "multipart/form-data",
                    schema = @Schema(implementation = ProductUpdateRequestDTO.class)
            )
    )
    @PostMapping("/update")
    public ResponseData<Object> updateProduct(@ModelAttribute ProductUpdateRequestDTO dto){
        log.info("Update product with product name", dto.getName());
        try{
            log.info("Update product with product name", dto.getName());
            return new ResponseData<>(HttpStatus.ACCEPTED.value(), "Product updated successfully", productService.updateProduct(dto));
        }catch (Exception e){
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Operation(summary = "Product detail", description = "Detail of Product")
    @GetMapping("/detail/{id}")
    public ResponseData<Object> getProductDetail(@PathVariable UUID id){
        log.info("Product detail");
        try {
            log.info("Product detail");
            return new ResponseData<>(HttpStatus.ACCEPTED.value(), "Product found successfully", productService.getProductById(id));
        } catch (Exception e) {
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Operation(summary = "Delete Product",description = "Delete Product")
    @DeleteMapping("/delete/{id}")
    public ResponseData<Object> deleteProduct(@PathVariable UUID id){
        log.info("Delete product with product id", id);
        try{
            log.info("Delete product with product id", id);
            return new ResponseData<>(HttpStatus.ACCEPTED.value(), "Product deleted successfully", productService.deleteProduct(id));
        }catch (Exception e){
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }
}
