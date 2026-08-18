package com.matchguard.backend.service;

import com.matchguard.backend.dto.AiScamDetectionResult;
import com.matchguard.backend.dto.ProductRequestDto;
import com.matchguard.backend.dto.ProductResponseDto;
import com.matchguard.backend.entity.Product;
import com.matchguard.backend.entity.User;
import com.matchguard.backend.repository.ProductRepository;
import com.matchguard.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AiScamDetectionService aiScamDetectionService;

    @Transactional
    public ProductResponseDto createProduct(ProductRequestDto requestDto) {

        User seller = userRepository.findById(requestDto.getSellerId())
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        // AI scam detection
        AiScamDetectionResult aiResult =
                aiScamDetectionService.analyzeProduct(
                        requestDto.getTitle(),
                        requestDto.getDescription(),
                        requestDto.getPrice(),
                        requestDto.getSocialPostUrl()
                );

        // Create product with AI-generated trust metrics
        Product product = Product.builder()
                .seller(seller)
                .title(requestDto.getTitle())
                .description(requestDto.getDescription())
                .price(requestDto.getPrice())
                .socialPostUrl(requestDto.getSocialPostUrl())
                .trustScore(aiResult.getTrustScore())
                .scamAnalysisSummary(aiResult.getScamAnalysisSummary())
                .isVerifiedSafe(aiResult.getIsVerifiedSafe())
                .build();

        product = productRepository.save(product);

        return mapToDto(product);
    }

    public List<ProductResponseDto> getProductsBySeller(Long sellerId) {
        List<Product> products = productRepository.findBySellerId(sellerId);

        return products.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private ProductResponseDto mapToDto(Product product) {

        return ProductResponseDto.builder()
                .id(product.getId())
                .sellerId(product.getSeller().getId())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .socialPostUrl(product.getSocialPostUrl())
                .trustScore(product.getTrustScore())
                .scamAnalysisSummary(product.getScamAnalysisSummary())
                .isVerifiedSafe(product.getIsVerifiedSafe())
                .createdAt(product.getCreatedAt())
                .build();
    }
}