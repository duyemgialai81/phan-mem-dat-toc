package com.example.demo.core.hairService.controller;

import com.example.demo.core.category.modal.response.CategoryResponse;
import com.example.demo.core.hairService.modal.request.ServiceResponse;
import com.example.demo.core.hairService.service.ServiceCategoryService;
import com.example.demo.uitl.ResponseObject;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api-v1/public")
@RequiredArgsConstructor
public class PublicController {
    private final ServiceCategoryService serviceCategoryService;
    @GetMapping("/categories")
    public ResponseEntity<?> getAllCategories() {
        List<CategoryResponse> categories = serviceCategoryService.getAllCategoriesWithServices();
        return ResponseEntity.ok(new ResponseObject<>(categories, "Lấy danh sách danh mục thành công"));
    }
    @GetMapping("/services")
    public ResponseEntity<?> getAllServices() {
        List<ServiceResponse> services = serviceCategoryService.getAllServices();
        return ResponseEntity.ok(new ResponseObject<>(services, "Lấy danh sách dịch vụ thành công"));
    }
    @GetMapping("/categories/{categoryId}/services")
    public ResponseEntity<?> getServicesByCategory(@PathVariable Long categoryId) {
        List<ServiceResponse> services = serviceCategoryService.getServicesByCategory(categoryId);
        return ResponseEntity.ok(new ResponseObject<>(services, "Lấy danh sách dịch vụ thành công"));
    }
}
