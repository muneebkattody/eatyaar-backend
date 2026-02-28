package com.eatyaar.controller;

import com.eatyaar.dto.response.RatingResponse;
import com.eatyaar.dto.response.UserProfileResponse;
import com.eatyaar.entity.User;
import com.eatyaar.repository.UserRepository;
import com.eatyaar.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final RatingService ratingService;

    // GET /api/users/me — Get my own profile
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(toProfileResponse(currentUser));
    }

    // GET /api/users/{id}/profile — Get any user's public profile
    @GetMapping("/{id}/profile")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(toProfileResponse(user));
    }

    // GET /api/users/{id}/ratings — Get ratings received by a user
    @GetMapping("/{id}/ratings")
    public ResponseEntity<List<RatingResponse>> getUserRatings(@PathVariable Long id) {
        return ResponseEntity.ok(ratingService.getRatingsForUser(id));
    }

    private UserProfileResponse toProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .city(user.getCity())
                .area(user.getArea())
                .trustScore(user.getTrustScore())
                .totalGiven(user.getTotalGiven())
                .totalTaken(user.getTotalTaken())
                .build();
    }
}
