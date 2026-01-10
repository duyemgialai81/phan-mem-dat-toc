package com.example.demo.core.appointment.service;


import com.example.demo.core.appointment.modal.request.AppointmentBookingRequest;
import com.example.demo.core.appointment.modal.response.AppointmentResponse;
import com.example.demo.entity.Appointment;
import com.example.demo.entity.Barber;
import com.example.demo.entity.HairService;
import com.example.demo.entity.User;
import com.example.demo.expection.ApiException;
import com.example.demo.repository.AppointmentRepository;
import com.example.demo.repository.BarberRepository;
import com.example.demo.repository.HairServiceRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final BarberRepository barberRepository;
    private final HairServiceRepository hairServiceRepository;

    @Transactional
    public AppointmentResponse bookAppointment(Long userId, AppointmentBookingRequest request) {
        User customer = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("Không tìm thấy người dùng", "USER_NOT_FOUND"));

        Barber barber = barberRepository.findById(request.getBarberId())
                .orElseThrow(() -> new ApiException("Không tìm thấy thợ cắt tóc", "BARBER_NOT_FOUND"));

        HairService hairService = hairServiceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ApiException("Không tìm thấy dịch vụ", "SERVICE_NOT_FOUND"));

        Appointment appointment = new Appointment();
        appointment.setCustomer(customer);
        appointment.setBarber(barber);
        appointment.setHairService(hairService);
        appointment.setBookingTime(request.getBookingTime());
        appointment.setNote(request.getNote());
        appointment.setStatus("PENDING");

        Appointment saved = appointmentRepository.save(appointment);
        return mapToResponse(saved);
    }

    public List<AppointmentResponse> getMyAppointments(Long userId) {
        User customer = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("Không tìm thấy người dùng", "USER_NOT_FOUND"));

        return appointmentRepository.findByCustomerOrderByBookingTimeDesc(customer)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<AppointmentResponse> getAppointmentsByStatus(Long userId, String status) {
        User customer = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("Không tìm thấy người dùng", "USER_NOT_FOUND"));

        return appointmentRepository.findByCustomerAndStatus(customer, status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AppointmentResponse cancelAppointment(Long userId, Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ApiException("Không tìm thấy lịch hẹn", "APPOINTMENT_NOT_FOUND"));

        if (!appointment.getCustomer().getId().equals(userId)) {
            throw new ApiException("Bạn không có quyền hủy lịch hẹn này", "FORBIDDEN");
        }

        if ("COMPLETED".equals(appointment.getStatus()) || "CANCELLED".equals(appointment.getStatus())) {
            throw new ApiException("Không thể hủy lịch hẹn đã hoàn thành hoặc đã hủy", "INVALID_STATUS");
        }

        appointment.setStatus("CANCELLED");
        Appointment updated = appointmentRepository.save(appointment);
        return mapToResponse(updated);
    }

    private AppointmentResponse mapToResponse(Appointment appointment) {
        AppointmentResponse response = new AppointmentResponse();
        response.setId(appointment.getId());
        response.setBookingTime(appointment.getBookingTime());
        response.setStatus(appointment.getStatus());
        response.setNote(appointment.getNote());
        response.setCustomerName(appointment.getCustomer().getFullName());
        response.setBarberName(appointment.getBarber().getUser().getFullName());
        response.setServiceName(appointment.getHairService().getName());
        response.setServicePrice(appointment.getHairService().getPrice());
        response.setServiceDuration(appointment.getHairService().getDurationMin());
        return response;
    }
}
