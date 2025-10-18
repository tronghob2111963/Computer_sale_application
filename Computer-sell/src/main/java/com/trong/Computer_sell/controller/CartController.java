package com.trong.Computer_sell.controller;

import com.trong.Computer_sell.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;


    @Operation(summary = "Add to cart", description = "Add product to cart")
    @PostMapping("/add")
    @PreAuthorize("hasAnyAuthority('SysAdmin','Admin', 'Staff','User')")
    public ResponseEntity<?> addToCart(@RequestParam UUID userId,
                                       @RequestParam UUID productId,
                                       @RequestParam(defaultValue = "1") int quantity) {
        try {
            return ResponseEntity.ok(cartService.addToCart(userId, productId, quantity));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "View cart", description = "View cart")
    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyAuthority('SysAdmin','Admin', 'Staff','User')")
    public ResponseEntity<?> viewCart(@PathVariable UUID userId) {
        try {
            return ResponseEntity.ok(cartService.viewCart(userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Update quantity", description = "Update quantity")
    @PutMapping("/update")
    @PreAuthorize("hasAnyAuthority('SysAdmin','Admin', 'Staff','User')")
    public ResponseEntity<?> updateQuantity(@RequestParam UUID userId,
                                            @RequestParam UUID productId,
                                            @RequestParam int quantity) {
        try {
            return ResponseEntity.ok(cartService.updateQuantity(userId, productId, quantity));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Remove item", description = "Remove item")
    @DeleteMapping("/remove")
    @PreAuthorize("hasAnyAuthority('SysAdmin','Admin', 'Staff','User')")
    public ResponseEntity<?> removeItem(@RequestParam UUID userId,
                                        @RequestParam UUID productId) {
        try {
            return ResponseEntity.ok(cartService.removeItem(userId, productId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
