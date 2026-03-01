package com.eatyaar.service;

import com.eatyaar.dto.request.ClaimRequest;
import com.eatyaar.dto.response.ClaimResponse;
import com.eatyaar.entity.Claim;
import com.eatyaar.entity.Claim.ClaimStatus;
import com.eatyaar.entity.FoodListing;
import com.eatyaar.entity.FoodListing.ListingStatus;
import com.eatyaar.entity.User;
import com.eatyaar.repository.ClaimRepository;
import com.eatyaar.repository.FoodListingRepository;
import com.eatyaar.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClaimService Tests")
class ClaimServiceTest {

    @Mock private ClaimRepository claimRepository;
    @Mock private FoodListingRepository listingRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private ClaimService claimService;

    private User giver;
    private User taker;
    private FoodListing listing;
    private Claim pendingClaim;

    @BeforeEach
    void setUp() {
        giver = User.builder()
                .id(1L).name("Giver").phone("9000000001")
                .totalGiven(0).totalTaken(0).trustScore(0.0).isVerified(true)
                .build();

        taker = User.builder()
                .id(2L).name("Taker").phone("9000000002")
                .totalGiven(0).totalTaken(0).trustScore(0.0).isVerified(true)
                .build();

        listing = FoodListing.builder()
                .id(10L)
                .postedBy(giver)
                .title("Wedding Biryani")
                .servings(10)
                .areaName("Koregaon Park")
                .exactAddress("Flat 4B, Sunrise Apts")
                .city("Pune").state("Maharashtra").pincode("411001")
                .status(ListingStatus.AVAILABLE)
                .foodType(FoodListing.FoodType.NON_VEG)
                .cookedAt(LocalDateTime.now().minusHours(1))
                .pickupBy(LocalDateTime.now().plusHours(3))
                .build();

        pendingClaim = Claim.builder()
                .id(100L)
                .listing(listing)
                .claimedBy(taker)
                .status(ClaimStatus.PENDING)
                .build();
    }

    // ─── claimListing ───────────────────────────────────────────

    @Test
    @DisplayName("claimListing: success — creates pending claim")
    void claimListing_success() {
        ClaimRequest request = new ClaimRequest();
        request.setListingId(10L);

        when(listingRepository.findById(10L)).thenReturn(Optional.of(listing));
        when(claimRepository.existsByListingAndClaimedBy(listing, taker)).thenReturn(false);
        when(claimRepository.save(any(Claim.class))).thenAnswer(inv -> {
            Claim c = inv.getArgument(0);
            c.setId(100L);
            return c;
        });

        ClaimResponse response = claimService.claimListing(taker, request);

        assertThat(response.getStatus()).isEqualTo(ClaimStatus.PENDING);
        assertThat(response.getListingId()).isEqualTo(10L);
        verify(claimRepository).save(any(Claim.class));
    }

    @Test
    @DisplayName("claimListing: giver cannot claim own listing")
    void claimListing_ownerCannotClaim() {
        ClaimRequest request = new ClaimRequest();
        request.setListingId(10L);

        when(listingRepository.findById(10L)).thenReturn(Optional.of(listing));

        // giver tries to claim their own listing
        assertThatThrownBy(() -> claimService.claimListing(giver, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("cannot claim your own listing");
    }

    @Test
    @DisplayName("claimListing: listing not available — throws")
    void claimListing_notAvailable_throws() {
        listing.setStatus(ListingStatus.CLAIMED);
        ClaimRequest request = new ClaimRequest();
        request.setListingId(10L);

        when(listingRepository.findById(10L)).thenReturn(Optional.of(listing));

        assertThatThrownBy(() -> claimService.claimListing(taker, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("no longer available");
    }

    @Test
    @DisplayName("claimListing: duplicate claim — throws")
    void claimListing_duplicateClaim_throws() {
        ClaimRequest request = new ClaimRequest();
        request.setListingId(10L);

        when(listingRepository.findById(10L)).thenReturn(Optional.of(listing));
        when(claimRepository.existsByListingAndClaimedBy(listing, taker)).thenReturn(true);

        assertThatThrownBy(() -> claimService.claimListing(taker, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already claimed");
    }

    @Test
    @DisplayName("claimListing: listing not found — throws")
    void claimListing_notFound_throws() {
        ClaimRequest request = new ClaimRequest();
        request.setListingId(999L);

        when(listingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> claimService.claimListing(taker, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    // ─── approveClaim ────────────────────────────────────────────

    @Test
    @DisplayName("approveClaim: success — status becomes APPROVED, listing becomes CLAIMED")
    void approveClaim_success() {
        when(claimRepository.findById(100L)).thenReturn(Optional.of(pendingClaim));
        when(claimRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(listingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ClaimResponse response = claimService.approveClaim(100L, giver);

        assertThat(response.getStatus()).isEqualTo(ClaimStatus.APPROVED);
        assertThat(listing.getStatus()).isEqualTo(ListingStatus.CLAIMED);
    }

    @Test
    @DisplayName("approveClaim: non-owner cannot approve")
    void approveClaim_nonOwner_throws() {
        when(claimRepository.findById(100L)).thenReturn(Optional.of(pendingClaim));

        assertThatThrownBy(() -> claimService.approveClaim(100L, taker))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("only the listing owner");
    }

    @Test
    @DisplayName("approveClaim: cannot approve already approved claim")
    void approveClaim_alreadyApproved_throws() {
        pendingClaim.setStatus(ClaimStatus.APPROVED);
        when(claimRepository.findById(100L)).thenReturn(Optional.of(pendingClaim));

        assertThatThrownBy(() -> claimService.approveClaim(100L, giver))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Only pending claims");
    }

    // ─── rejectClaim ─────────────────────────────────────────────

    @Test
    @DisplayName("rejectClaim: success — status becomes REJECTED")
    void rejectClaim_success() {
        when(claimRepository.findById(100L)).thenReturn(Optional.of(pendingClaim));
        when(claimRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ClaimResponse response = claimService.rejectClaim(100L, giver);

        assertThat(response.getStatus()).isEqualTo(ClaimStatus.REJECTED);
        // Listing should remain AVAILABLE after rejection
        assertThat(listing.getStatus()).isEqualTo(ListingStatus.AVAILABLE);
    }

    @Test
    @DisplayName("rejectClaim: non-owner cannot reject")
    void rejectClaim_nonOwner_throws() {
        when(claimRepository.findById(100L)).thenReturn(Optional.of(pendingClaim));

        assertThatThrownBy(() -> claimService.rejectClaim(100L, taker))
                .isInstanceOf(RuntimeException.class);
    }

    // ─── markPickedUp ────────────────────────────────────────────

    @Test
    @DisplayName("markPickedUp: success — listing COMPLETED, counters incremented")
    void markPickedUp_success() {
        pendingClaim.setStatus(ClaimStatus.APPROVED);

        when(claimRepository.findById(100L)).thenReturn(Optional.of(pendingClaim));
        when(claimRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(listingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ClaimResponse response = claimService.markPickedUp(100L, taker);

        assertThat(response.getStatus()).isEqualTo(ClaimStatus.PICKED_UP);
        assertThat(listing.getStatus()).isEqualTo(ListingStatus.COMPLETED);
        assertThat(giver.getTotalGiven()).isEqualTo(1);
        assertThat(taker.getTotalTaken()).isEqualTo(1);
    }

    @Test
    @DisplayName("markPickedUp: non-claimer cannot mark as picked up")
    void markPickedUp_nonClaimer_throws() {
        pendingClaim.setStatus(ClaimStatus.APPROVED);
        when(claimRepository.findById(100L)).thenReturn(Optional.of(pendingClaim));

        // giver tries to mark as picked up
        assertThatThrownBy(() -> claimService.markPickedUp(100L, giver))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Only the claimer");
    }

    @Test
    @DisplayName("markPickedUp: cannot mark pending claim as picked up")
    void markPickedUp_pendingClaim_throws() {
        // claim is still PENDING, not APPROVED
        when(claimRepository.findById(100L)).thenReturn(Optional.of(pendingClaim));

        assertThatThrownBy(() -> claimService.markPickedUp(100L, taker))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Only approved claims");
    }
}