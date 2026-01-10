package com.example.demo.core.appointment.modal.request;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppointmentBookingRequest {
    private LocalDateTime bookingTime;
    private String note;
    private Long barberId;
    private Long serviceId;
}