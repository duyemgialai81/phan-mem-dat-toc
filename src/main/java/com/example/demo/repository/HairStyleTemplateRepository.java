package com.example.demo.repository;
import com.example.demo.entity.HairStyleTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HairStyleTemplateRepository extends JpaRepository<HairStyleTemplate, Long> {
    List<HairStyleTemplate> findByIsActiveTrue();
    List<HairStyleTemplate> findByGenderAndIsActiveTrue(String gender);
    List<HairStyleTemplate> findByGenderAndLengthAndIsActiveTrue(String gender, String length);
}