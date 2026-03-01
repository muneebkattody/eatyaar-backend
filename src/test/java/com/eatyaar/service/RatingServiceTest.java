package com.eatyaar.service;

import com.eatyaar.dto.request.RatingRequest;
import com.eatyaar.dto.response.RatingResponse;
import com.eatyaar.entity.Claim;
import com.eatyaar.entity.Claim.ClaimStatus;
import com.eatyaar.entity.FoodListing;
import com.eatyaar.entity.Rating;
import com.eatyaar.entity.User;
import com.eatyaar.repository.ClaimRepository;
import com.eatyaar.repository.RatingRepository;
import com.eatyaar.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RatingService Tests")
class RatingServiceTest {

    @Mock private RatingRepository ratingRepository;
    @Mock private ClaimRepository claimRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private RatingService ratingService;

    private User giver;
    private User taker;
    private FoodListing listing;
    private Claim pickedUpClaim;

    @BeforeEach
    void setUp() {
        giver = User.builder().id(1L).name("Giver")
                .trustScore(0.0).totalGiven(1).isVerified(true).build();

        taker = User.builder().id(2L).name("Taker")
                .totalTaken(1).isVerified(true).build();

        listing = FoodListing.builder().id(10L).postedBy(giver)
                .title("Biryani").areaName("Pune").build();

        pickedUpClaim = Claim.builder()
                .id(100L).listing(listing)
                .claimedBy(taker).status(ClaimStatus.PICKED_UP)
                .build();
    }

    @Test
    @DisplayName("submitRating: success — rating saved and trust score updated")
    void submitRating_success() {
        RatingRequest request = new RatingRequest();
        request.setClaimId(100L);
        request.setScore(5);
        request.setComment("Great food!");

        when(claimRepository.findById(100L)).thenReturn(Optional.of(pickedUpClaim));
        when(ratingRepository.existsByListingAndGivenBy(listing, taker)).thenReturn(false);
        when(ratingRepository.save(any())).thenAnswer(inv -> {
            Rating r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });
        when(ratingRepository.calculateAverageScore(giver)).thenReturn(5.0);
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RatingResponse response = ratingService.submitRating(taker, request);

        assertThat(response.getScore()).isEqualTo(5);
        assertThat(response.getComment()).isEqualTo("Great food!");
        assertThat(giver.getTrustScore()).isEqualTo(5.0);
        verify(ratingRepository).save(any(Rating.class));
        verify(userRepository).save(giver);
    }

    @Test
    @DisplayName("submitRating: non-claimer cannot submit rating")
    void submitRating_nonClaimer_throws() {
        RatingRequest request = new RatingRequest();
        request.setClaimId(100L);
        request.setScore(4);

        when(claimRepository.findById(100L)).thenReturn(Optional.of(pickedUpClaim));

        assertThatThrownBy(() -> ratingService.submitRating(giver, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Only the claimer");
    }

    @Test
    @DisplayName("submitRating: cannot rate before pickup")
    void submitRating_notPickedUp_throws() {
        pickedUpClaim.setStatus(ClaimStatus.APPROVED); // not yet picked up
        RatingRequest request = new RatingRequest();
        request.setClaimId(100L);
        request.setScore(3);

        when(claimRepository.findById(100L)).thenReturn(Optional.of(pickedUpClaim));

        assertThatThrownBy(() -> ratingService.submitRating(taker, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("only rate after picking up");
    }

    @Test
    @DisplayName("submitRating: duplicate rating throws")
    void submitRating_duplicate_throws() {
        RatingRequest request = new RatingRequest();
        request.setClaimId(100L);
        request.setScore(4);

        when(claimRepository.findById(100L)).thenReturn(Optional.of(pickedUpClaim));
        when(ratingRepository.existsByListingAndGivenBy(listing, taker)).thenReturn(true);

        assertThatThrownBy(() -> ratingService.submitRating(taker, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already rated");
    }

    @Test
    @DisplayName("submitRating: trust score rounded to 1 decimal")
    void submitRating_trustScoreRounded() {
        RatingRequest request = new RatingRequest();
        request.setClaimId(100L);
        request.setScore(4);

        when(claimRepository.findById(100L)).thenReturn(Optional.of(pickedUpClaim));
        when(ratingRepository.existsByListingAndGivenBy(listing, taker)).thenReturn(false);
        when(ratingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(ratingRepository.calculateAverageScore(giver)).thenReturn(4.333333);
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ratingService.submitRating(taker, request);

        assertThat(giver.getTrustScore()).isEqualTo(4.3); // rounded to 1 decimal
    }
}