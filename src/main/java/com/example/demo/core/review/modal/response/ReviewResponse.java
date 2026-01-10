package com.example.demo.core.review.modal.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewResponse {
    private Long id;
    private Integer star;
    private String comment;
    private Long appointmentId;
    private String serviceName;
    private String barberName;
    private LocalDateTime appointmentDate;
}