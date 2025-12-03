package com.trong.Computer_sell.controller;


import com.trong.Computer_sell.DTO.request.product.ProductRequestDTO;
import com.trong.Computer_sell.DTO.request.product.ProductUpdateRequestDTO;
import com.trong.Computer_sell.DTO.response.common.ResponseData;
import com.trong.Computer_sell.DTO.response.common.ResponseError;
import com.trong.Computer_sell.common.ProductStatus;
import com.trong.Computer_sell.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @Operation(summary = "Product List by brand id", description = "Product List by brand id")
    @GetMapping("/list/brand/{brandId}")
    public ResponseData<Object> listProductByBrandId(
            @PathVariable UUID brandId,
            @RequestParam(required = false) String keyword ,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy
    ){
        log.info("List product by brand id");
        try {
            log.info("List product by brand id");
            return new ResponseData<>(HttpStatus.ACCEPTED.value(), "Product found successfully", productService.getAllProductsByBrandId(brandId, keyword, page, size, sortBy));
        } catch (Exception e) {
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }



    @Operation(summary = "fillter by productTye", description = "fillter by productTye")
    @GetMapping("/filter/product-type/{productTypeId}")
    public ResponseData<Object> filterProductByProductTypeId(
            @PathVariable UUID productTypeId,
            @RequestParam(required = false) String keyword ,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy){
        log.info("Filter product by product type id");
        try {
            log.info("Filter product by product type id");
            return new ResponseData<>(HttpStatus.ACCEPTED.value(), "Product found successfully", productService.getAllProductByProductTypeId(productTypeId, keyword, page, size, sortBy));
        } catch (Exception e) {
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }


    @Operation(summary = "Product List by category id", description = "Product List by category id")
    @GetMapping("/list/category/{categoryId}")
    public ResponseData<Object> listProductByCategoryId(
            @PathVariable UUID categoryId,
            @RequestParam(required = false) String keyword ,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy
    ){
        log.info("List product by category id");
        try {
            log.info("List product by category id");
            return new ResponseData<>(HttpStatus.ACCEPTED.value(), "Product found successfully", productService.getAllProductsByCategoryId(categoryId, keyword, page, size, sortBy));
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

    @Operation(summary = "Delete Product (Hard Delete)", description = "Xóa cứng sản phẩm - Không khuyến khích sử dụng")
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('SysAdmin','Admin')")
    public ResponseData<Object> deleteProduct(@PathVariable UUID id){
        log.info("Delete product with product id", id);
        try{
            log.info("Delete product with product id", id);
            return new ResponseData<>(HttpStatus.ACCEPTED.value(), "Product deleted successfully", productService.deleteProduct(id));
        }catch (Exception e){
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    // ==================== SOFT DELETE & STATUS MANAGEMENT ====================

    @Operation(summary = "Soft Delete Product", description = "Xóa mềm sản phẩm - Chuyển trạng thái sang DELETED")
    @PutMapping("/soft-delete/{id}")
    @PreAuthorize("hasAnyAuthority('SysAdmin','Admin')")
    public ResponseData<Object> softDeleteProduct(@PathVariable UUID id){
        log.info("Soft delete product with id: {}", id);
        try{
            productService.softDeleteProduct(id);
            return new ResponseData<>(HttpStatus.OK.value(), "Product soft deleted successfully", null);
        }catch (Exception e){
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Operation(summary = "Restore Product", description = "Khôi phục sản phẩm đã xóa mềm")
    @PutMapping("/restore/{id}")
    @PreAuthorize("hasAnyAuthority('SysAdmin','Admin')")
    public ResponseData<Object> restoreProduct(@PathVariable UUID id){
        log.info("Restore product with id: {}", id);
        try{
            productService.restoreProduct(id);
            return new ResponseData<>(HttpStatus.OK.value(), "Product restored successfully", null);
        }catch (Exception e){
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Operation(summary = "Update Product Status", description = "Cập nhật trạng thái sản phẩm (ACTIVE/INACTIVE/DELETED)")
    @PutMapping("/status/{id}")
    @PreAuthorize("hasAnyAuthority('SysAdmin','Admin')")
    public ResponseData<Object> updateProductStatus(
            @PathVariable UUID id,
            @RequestParam ProductStatus status){
        log.info("Update product {} status to {}", id, status);
        try{
            productService.updateProductStatus(id, status);
            return new ResponseData<>(HttpStatus.OK.value(), "Product status updated successfully", null);
        }catch (Exception e){
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }
}
