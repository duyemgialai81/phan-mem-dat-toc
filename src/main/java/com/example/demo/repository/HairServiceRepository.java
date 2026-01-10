package com.example.demo.repository;

import com.example.demo.entity.Category;
import com.example.demo.entity.HairService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HairServiceRepository extends JpaRepository<HairService, Long> {
    List<HairService> findByCategory(Category category);
}
