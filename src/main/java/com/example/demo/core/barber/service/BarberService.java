package com.example.demo.core.barber.service;

import com.example.demo.core.barber.modal.response.BarberResponse;

import com.example.demo.entity.Barber;
import com.example.demo.repository.BarberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BarberService {

    private final BarberRepository barberRepository;

    public List<BarberResponse> getAllBarbers() {
        return barberRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public BarberResponse getBarberById(Long id) {
        Barber barber = barberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thợ cắt tóc"));
        return mapToResponse(barber);
    }

    private BarberResponse mapToResponse(Barber barber) {
        BarberResponse response = new BarberResponse();
        response.setId(barber.getId());
        response.setName(barber.getUser().getFullName());
        response.setExperience(barber.getExperience());
        response.setRating(barber.getRating());
        return response;
    }
}
