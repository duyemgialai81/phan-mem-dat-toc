package com.example.demo.core.review.modal.request;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class ReviewRequest {
    @NotNull(message = "Appointment ID không được để trống")
    private Long appointmentId;

    @NotNull(message = "Số sao không được để trống")
    @Min(value = 1, message = "Số sao phải từ 1-5")
    @Max(value = 5, message = "Số sao phải từ 1-5")
    private Integer star;

    private String comment;
}