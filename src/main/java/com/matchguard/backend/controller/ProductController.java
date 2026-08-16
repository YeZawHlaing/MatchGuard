package com.matchguard.backend.controller;

import com.matchguard.backend.dto.ProductRequestDto;
import com.matchguard.backend.dto.ProductResponseDto;
import com.matchguard.backend.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping("/seller")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ProductResponseDto> createProduct(@RequestBody ProductRequestDto requestDto) {
        ProductResponseDto createdProduct = productService.createProduct(requestDto);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    @GetMapping("/seller/{sellerId}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<List<ProductResponseDto>> getProductsBySeller(@PathVariable Long sellerId) {
        List<ProductResponseDto> products = productService.getProductsBySeller(sellerId);
        return ResponseEntity.ok(products);
    }
}
