package com.example.demo.repository;

import com.example.demo.entity.UserPromotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPromotionRepository extends JpaRepository<UserPromotion, Long> {
    boolean existsByUserIdAndPromotionId(Long userId, Long promotionId);
    Optional<UserPromotion> findByUserIdAndPromotionId(Long userId, Long promotionId);
}