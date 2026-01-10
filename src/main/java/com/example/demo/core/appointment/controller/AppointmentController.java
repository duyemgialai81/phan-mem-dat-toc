package com.example.demo.core.appointment.controller;


import com.example.demo.core.appointment.modal.request.AppointmentBookingRequest;
import com.example.demo.core.appointment.modal.response.AppointmentResponse;
import com.example.demo.core.appointment.service.AppointmentService;
import com.example.demo.security.JwtTokenProvider;

import com.example.demo.uitl.ResponseObject;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api-v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final JwtTokenProvider jwtTokenProvider;

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            String userId = jwtTokenProvider.getUserIdFromToken(token);
            return Long.parseLong(userId);
        }
        throw new RuntimeException("Token không hợp lệ");
    }

    @PostMapping("/book")
    public ResponseEntity<?> bookAppointment(
            @RequestBody AppointmentBookingRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        AppointmentResponse response = appointmentService.bookAppointment(userId, request);
        return ResponseEntity.ok(new ResponseObject<>(response, "Đặt lịch thành công"));
    }

    @GetMapping("/my-appointments")
    public ResponseEntity<?> getMyAppointments(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        List<AppointmentResponse> appointments = appointmentService.getMyAppointments(userId);
        return ResponseEntity.ok(new ResponseObject<>(appointments, "Lấy danh sách lịch hẹn thành công"));
    }

    @GetMapping("/my-appointments/status/{status}")
    public ResponseEntity<?> getAppointmentsByStatus(
            @PathVariable String status,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        List<AppointmentResponse> appointments = appointmentService.getAppointmentsByStatus(userId, status);
        return ResponseEntity.ok(new ResponseObject<>(appointments, "Lấy danh sách lịch hẹn thành công"));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelAppointment(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        AppointmentResponse response = appointmentService.cancelAppointment(userId, id);
        return ResponseEntity.ok(new ResponseObject<>(response, "Hủy lịch hẹn thành công"));
    }
}