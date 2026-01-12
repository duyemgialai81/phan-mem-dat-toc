package com.example.demo.core.review.modal.request;

import lombok.Data;

@Data
public class ReviewRequest {
    private Long appointmentId;
    private Integer star;
    private String comment;
}