package com.example.demo.repository;

import com.example.demo.entity.Appointment;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByCustomerOrderByBookingTimeDesc(User customer);
    List<Appointment> findByCustomerAndStatus(User customer, String status);
}
