package com.example.demo.core.review.service;


import com.example.demo.core.review.modal.request.ReviewRequest;
import com.example.demo.core.review.modal.response.ReviewResponse;
import com.example.demo.entity.Appointment;
import com.example.demo.entity.Review;
import com.example.demo.expection.ApiException;
import com.example.demo.repository.AppointmentRepository;
import com.example.demo.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final AppointmentRepository appointmentRepository;

    @Transactional
    public ReviewResponse createReview(Long userId, ReviewRequest request) {
        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new ApiException("Không tìm thấy lịch hẹn", "APPOINTMENT_NOT_FOUND"));

        if (!appointment.getCustomer().getId().equals(userId)) {
            throw new ApiException("Bạn không có quyền đánh giá lịch hẹn này", "FORBIDDEN");
        }

        if (!"COMPLETED".equals(appointment.getStatus())) {
            throw new ApiException("Chỉ có thể đánh giá lịch hẹn đã hoàn thành", "INVALID_STATUS");
        }

        if (reviewRepository.findByAppointment(appointment).isPresent()) {
            throw new ApiException("Lịch hẹn này đã được đánh giá", "ALREADY_REVIEWED");
        }

        Review review = new Review();
        review.setAppointment(appointment);
        review.setStar(request.getStar());
        review.setComment(request.getComment());

        Review saved = reviewRepository.save(review);
        return mapToResponse(saved);
    }

    public List<ReviewResponse> getMyReviews(Long userId) {
        return reviewRepository.findByAppointment_Customer_IdOrderByIdDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ReviewResponse mapToResponse(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setStar(review.getStar());
        response.setComment(review.getComment());
        response.setAppointmentId(review.getAppointment().getId());
        response.setServiceName(review.getAppointment().getHairService().getName());
        response.setBarberName(review.getAppointment().getBarber().getUser().getFullName());
        response.setAppointmentDate(review.getAppointment().getBookingTime());
        return response;
    }
}
