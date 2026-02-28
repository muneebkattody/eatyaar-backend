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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final FoodListingRepository listingRepository;
    private final UserRepository userRepository;

    // Taker claims a listing
    public ClaimResponse claimListing(User currentUser, ClaimRequest request) {
        FoodListing listing = listingRepository.findById(request.getListingId())
                .orElseThrow(() -> new RuntimeException("Listing not found"));

        // Can't claim your own listing
        if (listing.getPostedBy().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You cannot claim your own listing");
        }

        // Listing must be available
        if (listing.getStatus() != ListingStatus.AVAILABLE) {
            throw new RuntimeException("This listing is no longer available");
        }

        // Prevent duplicate claims
        if (claimRepository.existsByListingAndClaimedBy(listing, currentUser)) {
            throw new RuntimeException("You have already claimed this listing");
        }

        Claim claim = Claim.builder()
                .listing(listing)
                .claimedBy(currentUser)
                .build();

        return toResponse(claimRepository.save(claim));
    }

    // Get all claims made by current user
    public List<ClaimResponse> getMyClaims(User currentUser) {
        return claimRepository.findByClaimedBy(currentUser)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Giver approves a claim
    public ClaimResponse approveClaim(Long claimId, User currentUser) {
        Claim claim = getClaimAndVerifyOwner(claimId, currentUser);

        if (claim.getStatus() != ClaimStatus.PENDING) {
            throw new RuntimeException("Only pending claims can be approved");
        }

        claim.setStatus(ClaimStatus.APPROVED);

        // Mark listing as claimed so others can't claim it
        FoodListing listing = claim.getListing();
        listing.setStatus(ListingStatus.CLAIMED);
        listingRepository.save(listing);

        return toResponse(claimRepository.save(claim));
    }

    // Giver rejects a claim
    public ClaimResponse rejectClaim(Long claimId, User currentUser) {
        Claim claim = getClaimAndVerifyOwner(claimId, currentUser);

        if (claim.getStatus() != ClaimStatus.PENDING) {
            throw new RuntimeException("Only pending claims can be rejected");
        }

        claim.setStatus(ClaimStatus.REJECTED);
        return toResponse(claimRepository.save(claim));
    }

    // Taker marks food as picked up
    public ClaimResponse markPickedUp(Long claimId, User currentUser) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found"));

        // Only the claimer can mark as picked up
        if (!claim.getClaimedBy().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only the claimer can mark this as picked up");
        }

        if (claim.getStatus() != ClaimStatus.APPROVED) {
            throw new RuntimeException("Only approved claims can be marked as picked up");
        }

        claim.setStatus(ClaimStatus.PICKED_UP);

        // Mark listing as completed
        FoodListing listing = claim.getListing();
        listing.setStatus(ListingStatus.COMPLETED);
        listingRepository.save(listing);

        // Update giver's totalGiven count
        User giver = listing.getPostedBy();
        giver.setTotalGiven(giver.getTotalGiven() + 1);
        userRepository.save(giver);

        // Update taker's totalTaken count
        currentUser.setTotalTaken(currentUser.getTotalTaken() + 1);
        userRepository.save(currentUser);

        return toResponse(claimRepository.save(claim));
    }

    // Helper — find claim and verify current user is the listing owner
    private Claim getClaimAndVerifyOwner(Long claimId, User currentUser) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found"));

        if (!claim.getListing().getPostedBy().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only the listing owner can perform this action");
        }

        return claim;
    }

    // Convert entity to response DTO
    private ClaimResponse toResponse(Claim claim) {
        boolean addressVisible = claim.getStatus() == ClaimStatus.APPROVED
                || claim.getStatus() == ClaimStatus.PICKED_UP;

        return ClaimResponse.builder()
                .id(claim.getId())
                .status(claim.getStatus())
                .createdAt(claim.getCreatedAt())
                .listingId(claim.getListing().getId())
                .listingTitle(claim.getListing().getTitle())
                .listingAreaName(claim.getListing().getAreaName())
                .exactAddress(addressVisible
                        ? claim.getListing().getExactAddress()
                        : "Address revealed after approval")
                .claimedById(claim.getClaimedBy().getId())
                .claimedByName(claim.getClaimedBy().getName())
                .build();
    }

    // Get all claims received on giver's listings
    public List<ClaimResponse> getReceivedClaims(User currentUser) {
        return claimRepository.findClaimsReceivedByUser(currentUser)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

}
