package com.matchguard.backend.repository;

import com.matchguard.backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Fetch all social media product listings for a specific shop owner
    List<Product> findBySellerId(Long sellerId);

    // Optional: Fetch only items verified safe by the AI scam detector
    List<Product> findByIsVerifiedSafeTrue();
}
