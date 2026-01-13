package com.example.demo.repository;

import com.example.demo.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    List<Promotion> findByExpiryDateAfter(LocalDate date);
    Optional<Promotion> findByCode(String code);
    List<Promotion> findByExpiryDateGreaterThanEqual(LocalDate date);
}