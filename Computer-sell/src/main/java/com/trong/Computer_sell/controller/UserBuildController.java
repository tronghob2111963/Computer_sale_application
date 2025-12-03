package com.trong.Computer_sell.controller;


import com.trong.Computer_sell.DTO.request.user.UserBuildRequestDTO;
import com.trong.Computer_sell.DTO.response.common.ResponseData;
import com.trong.Computer_sell.DTO.response.common.ResponseError;
import com.trong.Computer_sell.service.UserBuildService;
import com.trong.Computer_sell.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/builds")
@RequiredArgsConstructor
@Slf4j(topic = "USER_BUILD_CONTROLLER")
@Tag(name = "User Build Controller", description = "User Build Controller")
public class UserBuildController {
    private final UserBuildService userBuildService;
    private final ProductService productService;


    @Operation(summary = "Create Build" , description = "Create a Pc build")
    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority('SysAdmin','Admin', 'Staff','User')")
    public ResponseData<Object> createBuild(@RequestBody UserBuildRequestDTO userBuild){
        log.info("Create build with build name", userBuild.getName());
        try{
            log.info("Create build with build name", userBuild.getName());
            return new ResponseData<>(HttpStatus.ACCEPTED.value(), "Build created successfully", userBuildService.createBuild(userBuild.getUserId(), userBuild.getName()));
        }catch (Exception e){
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Operation(summary = "Add product to build", description = "Add product to build")
    @PostMapping("/{buildId}/add-product")
    @PreAuthorize("hasAnyAuthority('SysAdmin','Admin', 'Staff','User')")
    public ResponseData<?> addProduct(@PathVariable UUID buildId,
                                        @RequestParam UUID productId,
                                        @RequestParam(defaultValue = "1") int quantity) {
        log.info("Add product to build with build id", buildId);
        try{
            log.info("Add product to build with build id", buildId);
            return new ResponseData<>(HttpStatus.ACCEPTED.value(), "Product added to build successfully", userBuildService.addProductToBuild(buildId, productId, quantity));
        }catch (Exception e){
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Operation(summary = "Remove product from build", description = "Remove product from build")
    @DeleteMapping("/{buildId}/remove-product/{productId}")
    @PreAuthorize("hasAnyAuthority('SysAdmin','Admin', 'Staff','User')")
    public ResponseData<?> removeProduct(@PathVariable UUID buildId , @PathVariable UUID productId) {
        try{
            log.info("Remove product from build with build id", buildId);
            return new ResponseData<>(HttpStatus.ACCEPTED.value(), "Product removed from build successfully", userBuildService.removeProductFromBuild(buildId, productId));
        }catch (Exception e){
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }


    @Operation(summary = "Get user builds", description = "Get user builds")
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyAuthority('SysAdmin','Admin', 'Staff','User')")
    public ResponseData<Object> getUserBuilds(@PathVariable UUID userId) {
        try{
            log.info("Get user builds with user id", userId);
            return new ResponseData<>(HttpStatus.ACCEPTED.value(), "Builds found successfully", userBuildService.getUserBuilds(userId));
        }catch (Exception e){
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }


    @Operation(summary = "Get build details", description = "Get build details")
    @GetMapping("/{buildId}")
    @PreAuthorize("hasAnyAuthority('SysAdmin','Admin', 'Staff','User')")
    public ResponseEntity<?> getBuildDetails(@PathVariable UUID buildId) {
        return ResponseEntity.ok(userBuildService.getBuild(buildId));
    }
    
    @Operation(summary = "Update product quantity in build", description = "Update product quantity in build")
    @PutMapping("/{buildId}/update-quantity")
    @PreAuthorize("hasAnyAuthority('SysAdmin','Admin', 'Staff','User')")
    public ResponseData<?> updateProductQuantity(@PathVariable UUID buildId,
                                                   @RequestParam UUID productId,
                                                   @RequestParam int quantity) {
        try{
            log.info("Update product quantity in build with build id: {}", buildId);
            return new ResponseData<>(HttpStatus.ACCEPTED.value(), "Product quantity updated successfully", 
                userBuildService.updateProductQuantity(buildId, productId, quantity));
        }catch (Exception e){
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }
    
    @Operation(summary = "Delete build", description = "Delete build")
    @DeleteMapping("/{buildId}")
    @PreAuthorize("hasAnyAuthority('SysAdmin','Admin', 'Staff','User')")
    public ResponseData<?> deleteBuild(@PathVariable UUID buildId) {
        try{
            log.info("Delete build with build id: {}", buildId);
            userBuildService.deleteBuild(buildId);
            return new ResponseData<>(HttpStatus.ACCEPTED.value(), "Build deleted successfully");
        }catch (Exception e){
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Operation(summary = "Get products by product type for PC Builder", description = "Get products by product type for PC Builder")
    @GetMapping("/products/by-type/{productTypeId}")
    public ResponseData<Object> getProductsByType(
            @PathVariable UUID productTypeId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy) {
        log.info("Get products by product type id: {}", productTypeId);
        try {
            return new ResponseData<>(HttpStatus.ACCEPTED.value(), "Products found successfully", 
                productService.getAllProductByProductTypeId(productTypeId, keyword, page, size, sortBy));
        } catch (Exception e) {
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }
}
