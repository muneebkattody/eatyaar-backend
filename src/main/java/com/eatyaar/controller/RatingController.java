package com.eatyaar.controller;

import com.eatyaar.dto.request.RatingRequest;
import com.eatyaar.dto.response.RatingResponse;
import com.eatyaar.dto.response.UserProfileResponse;
import com.eatyaar.entity.User;
import com.eatyaar.repository.UserRepository;
import com.eatyaar.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    // POST /api/ratings — Submit a rating after pickup
    @PostMapping
    public ResponseEntity<RatingResponse> submitRating(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody RatingRequest request) {
        return ResponseEntity.ok(ratingService.submitRating(currentUser, request));
    }
}