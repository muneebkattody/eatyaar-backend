package com.eatyaar.service;

import com.eatyaar.dto.request.RatingRequest;
import com.eatyaar.dto.response.RatingResponse;
import com.eatyaar.entity.Claim;
import com.eatyaar.entity.Claim.ClaimStatus;
import com.eatyaar.entity.Rating;
import com.eatyaar.entity.User;
import com.eatyaar.repository.ClaimRepository;
import com.eatyaar.repository.RatingRepository;
import com.eatyaar.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final ClaimRepository claimRepository;
    private final UserRepository userRepository;

    // Taker rates the giver after pickup
    public RatingResponse submitRating(User currentUser, RatingRequest request) {
        Claim claim = claimRepository.findById(request.getClaimId())
                .orElseThrow(() -> new RuntimeException("Claim not found"));

        // Only the taker can rate after pickup
        if (!claim.getClaimedBy().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only the claimer can submit a rating");
        }

        // Food must be picked up before rating
        if (claim.getStatus() != ClaimStatus.PICKED_UP) {
            throw new RuntimeException("You can only rate after picking up the food");
        }

        // Prevent duplicate ratings
        if (ratingRepository.existsByListingAndGivenBy(claim.getListing(), currentUser)) {
            throw new RuntimeException("You have already rated this listing");
        }

        User giver = claim.getListing().getPostedBy();

        Rating rating = Rating.builder()
                .givenBy(currentUser)
                .givenTo(giver)
                .listing(claim.getListing())
                .score(request.getScore())
                .comment(request.getComment())
                .build();

        ratingRepository.save(rating);

        // Recalculate and update giver's trust score
        Double avgScore = ratingRepository.calculateAverageScore(giver);
        if (avgScore != null) {
            giver.setTrustScore(Math.round(avgScore * 10.0) / 10.0); // round to 1 decimal
            userRepository.save(giver);
        }

        return toResponse(rating);
    }

    // Get all ratings for a user
    public List<RatingResponse> getRatingsForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ratingRepository.findByGivenTo(user)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Convert entity to response DTO
    private RatingResponse toResponse(Rating rating) {
        return RatingResponse.builder()
                .id(rating.getId())
                .score(rating.getScore())
                .comment(rating.getComment())
                .createdAt(rating.getCreatedAt())
                .givenById(rating.getGivenBy().getId())
                .givenByName(rating.getGivenBy().getName())
                .givenToId(rating.getGivenTo().getId())
                .givenToName(rating.getGivenTo().getName())
                .listingId(rating.getListing().getId())
                .listingTitle(rating.getListing().getTitle())
                .build();
    }
}
