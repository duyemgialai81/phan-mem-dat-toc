package com.example.demo.core.hairService.modal.request;

import lombok.Data;

@Data
public class ServiceResponse {
    private Long id;
    private String name;
    private Double price;
    private Integer durationMin;
    private String categoryName;
}
