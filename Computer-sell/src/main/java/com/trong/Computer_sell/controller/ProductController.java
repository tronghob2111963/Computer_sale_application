package com.trong.Computer_sell.controller;


import com.trong.Computer_sell.DTO.request.ProductRequestDTO;
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
}
