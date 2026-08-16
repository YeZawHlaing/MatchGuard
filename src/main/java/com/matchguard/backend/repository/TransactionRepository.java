package com.matchguard.backend.repository;


import com.matchguard.backend.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Fetch all transactions initiated by a specific buyer
    List<Transaction> findByBuyerId(Long buyerId);

    // Fetch incoming secured orders for the seller's dashboard by traversing the Product relation
    List<Transaction> findByProduct_Seller_Id(Long sellerId);

    // Find a transaction using the unique secure QR token for the release handshake
    Optional<Transaction> findByQrToken(String qrToken);
}