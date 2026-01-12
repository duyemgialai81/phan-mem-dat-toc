package com.example.demo.core.appointment.controller;

import com.example.demo.core.appointment.modal.request.AppointmentBookingRequest;
import com.example.demo.core.appointment.modal.response.AppointmentResponse;
import com.example.demo.core.appointment.service.AppointmentService;
import com.example.demo.uitl.ResponseObject;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api-v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    private Long getUserId(UserDetails userDetails) {
        return Long.parseLong(userDetails.getUsername());
    }

    @PostMapping("/book")
    public ResponseEntity<?> bookAppointment(
            @RequestBody AppointmentBookingRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        AppointmentResponse response = appointmentService.bookAppointment(userId, request);
        return ResponseEntity.ok(new ResponseObject<>(response, "Đặt lịch thành công"));
    }

    @GetMapping("/my-appointments")
    public ResponseEntity<?> getMyAppointments(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        List<AppointmentResponse> appointments = appointmentService.getMyAppointments(userId);
        return ResponseEntity.ok(new ResponseObject<>(appointments, "Lấy danh sách lịch hẹn thành công"));
    }

    @GetMapping("/my-appointments/status/{status}")
    public ResponseEntity<?> getAppointmentsByStatus(
            @PathVariable String status,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        List<AppointmentResponse> appointments = appointmentService.getAppointmentsByStatus(userId, status);
        return ResponseEntity.ok(new ResponseObject<>(appointments, "Lấy danh sách lịch hẹn thành công"));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelAppointment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        AppointmentResponse response = appointmentService.cancelAppointment(userId, id);
        return ResponseEntity.ok(new ResponseObject<>(response, "Hủy lịch hẹn thành công"));
    }
}