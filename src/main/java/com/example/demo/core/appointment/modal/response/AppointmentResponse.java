package com.example.demo.core.appointment.modal.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppointmentResponse {
    private Long id;
    private LocalDateTime bookingTime;
    private String status;
    private String note;
    private String customerName;
    private String barberName;
    private String serviceName;
    private Double servicePrice;
    private Integer serviceDuration;
}