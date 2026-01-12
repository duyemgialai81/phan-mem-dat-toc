package com.example.demo.repository;

import com.example.demo.entity.HairStyleSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HairStyleSuggestionRepository extends JpaRepository<HairStyleSuggestion, Long> {
    List<HairStyleSuggestion> findByUserIdOrderByCreatedAtDesc(Long userId);
}
