package com.example.demo.core.hairService.service;


import com.example.demo.core.category.modal.response.CategoryResponse;
import com.example.demo.core.hairService.modal.request.ServiceResponse;
import com.example.demo.entity.Category;
import com.example.demo.entity.HairService;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.HairServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceCategoryService {

    private final CategoryRepository categoryRepository;
    private final HairServiceRepository hairServiceRepository;

    public List<CategoryResponse> getAllCategoriesWithServices() {
        return categoryRepository.findAll().stream()
                .map(category -> {
                    CategoryResponse response = new CategoryResponse();
                    response.setId(category.getId());
                    response.setName(category.getName());

                    List<ServiceResponse> services = hairServiceRepository.findByCategory(category)
                            .stream()
                            .map(this::mapServiceToResponse)
                            .collect(Collectors.toList());

                    response.setServices(services);
                    return response;
                })
                .collect(Collectors.toList());
    }

    public List<ServiceResponse> getAllServices() {
        return hairServiceRepository.findAll().stream()
                .map(this::mapServiceToResponse)
                .collect(Collectors.toList());
    }

    public List<ServiceResponse> getServicesByCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        return hairServiceRepository.findByCategory(category).stream()
                .map(this::mapServiceToResponse)
                .collect(Collectors.toList());
    }

    private ServiceResponse mapServiceToResponse(HairService service) {
        ServiceResponse response = new ServiceResponse();
        response.setId(service.getId());
        response.setName(service.getName());
        response.setPrice(service.getPrice());
        response.setDurationMin(service.getDurationMin());
        response.setCategoryName(service.getCategory().getName());
        return response;
    }
}

