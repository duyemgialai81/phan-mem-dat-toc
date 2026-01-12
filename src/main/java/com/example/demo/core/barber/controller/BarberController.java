package com.example.demo.core.barber.controller;


import com.example.demo.core.barber.modal.response.BarberResponse;
import com.example.demo.core.barber.service.BarberService;
import com.example.demo.uitl.ResponseObject;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api-v1/barbers")
@RequiredArgsConstructor
public class BarberController {
    private final BarberService barberService;
    @GetMapping
    public ResponseEntity<?> getAllBarbers() {
        List<BarberResponse> barbers = barberService.getAllBarbers();
        return ResponseEntity.ok(new ResponseObject<>(barbers, "Lấy danh sách thợ cắt tóc thành công"));
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getBarberById(@PathVariable Long id) {
        BarberResponse barber = barberService.getBarberById(id);
        return ResponseEntity.ok(new ResponseObject<>(barber, "Lấy thông tin thợ cắt tóc thành công"));
    }
}