package com.example.demo.repository;

import com.example.demo.entity.Review;
import com.example.demo.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByAppointment(Appointment appointment);
    List<Review> findByAppointment_Customer_IdOrderByIdDesc(Long customerId);
}