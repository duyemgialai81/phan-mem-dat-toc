package com.example.demo.core.category.modal.response;

import com.example.demo.core.hairService.modal.request.ServiceResponse;
import lombok.Data;
import java.util.List;

@Data
public class CategoryResponse {
    private Long id;
    private String name;
    private List<ServiceResponse> services;
}