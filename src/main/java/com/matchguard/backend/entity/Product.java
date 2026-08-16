package com.matchguard.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private String socialPostUrl;

    @Column(name = "trust_score")
    private Integer trustScore;

    @Column(name = "scam_analysis_summary", columnDefinition = "TEXT")
    private String scamAnalysisSummary;

    @Column(name = "is_verified_safe")
    private Boolean isVerifiedSafe;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}