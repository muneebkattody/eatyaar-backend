package com.eatyaar.controller;

import com.eatyaar.entity.Claim.ClaimStatus;
import com.eatyaar.repository.ClaimRepository;
import com.eatyaar.repository.FoodListingRepository;
import com.eatyaar.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatusController {

    private final FoodListingRepository listingRepository;
    private final ClaimRepository claimRepository;
    private final UserRepository userRepository;

    // GET /api/stats/global — public, no auth needed
    @GetMapping("/global")
    public ResponseEntity<Map<String, Long>> getGlobalStats() {
        long totalListings  = listingRepository.count();
        long totalPickedUp  = claimRepository.countByStatus(ClaimStatus.PICKED_UP);
        long totalUsers     = userRepository.count();

        return ResponseEntity.ok(Map.of(
                "totalListings",  totalListings,
                "totalPickedUp",  totalPickedUp,
                "totalUsers",     totalUsers
        ));
    }
}
