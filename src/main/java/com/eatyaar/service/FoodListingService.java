package com.eatyaar.service;

import com.eatyaar.dto.request.CreateListingRequest;
import com.eatyaar.dto.response.ListingResponse;
import com.eatyaar.entity.FoodListing;
import com.eatyaar.entity.FoodListing.ListingStatus;
import com.eatyaar.entity.User;
import com.eatyaar.repository.FoodListingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FoodListingService {

    private final FoodListingRepository listingRepository;

    // Create a new food listing
    public ListingResponse createListing(User currentUser, CreateListingRequest request) {
        FoodListing listing = FoodListing.builder()
                .postedBy(currentUser)
                .title(request.getTitle())
                .description(request.getDescription())
                .servings(request.getServings())
                .foodType(request.getFoodType())
                .cookedAt(request.getCookedAt())
                .pickupBy(request.getPickupBy())
                .areaName(request.getAreaName())
                .exactAddress(request.getExactAddress())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .build();

        FoodListing saved = listingRepository.save(listing);
        return toResponse(saved, true); // owner sees full address
    }

    // Get all available listings in a city
    public List<ListingResponse> getListingsByCity(String city) {
        return listingRepository
                .findByCityAndStatus(city, ListingStatus.AVAILABLE)
                .stream()
                .map(l -> toResponse(l, false)) // public — hide exact address
                .collect(Collectors.toList());
    }

    // Get all available listings in an area
    public List<ListingResponse> getListingsByArea(String area) {
        return listingRepository
                .findByAreaNameAndStatus(area, ListingStatus.AVAILABLE)
                .stream()
                .map(l -> toResponse(l, false))
                .collect(Collectors.toList());
    }

    // Get a single listing by id
    public ListingResponse getListingById(Long id, boolean showAddress) {
        FoodListing listing = listingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Listing not found with id: " + id));
        return toResponse(listing, showAddress);
    }

    // Get listings posted by current user
    public List<ListingResponse> getMyListings(User currentUser) {
        return listingRepository
                .findByPostedBy(currentUser)
                .stream()
                .map(l -> toResponse(l, true)) // owner sees full address
                .collect(Collectors.toList());
    }

    // Mark listing as expired
    public ListingResponse expireListing(Long id, User currentUser) {
        FoodListing listing = listingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Listing not found with id: " + id));

        if (!listing.getPostedBy().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not the owner of this listing");
        }

        listing.setStatus(ListingStatus.EXPIRED);
        return toResponse(listingRepository.save(listing), true);
    }

    // Delete listing (owner only)
    public void deleteListing(Long id, User currentUser) {
        FoodListing listing = listingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Listing not found with id: " + id));

        if (!listing.getPostedBy().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not the owner of this listing");
        }

        listingRepository.delete(listing);
    }

    // Convert entity to response DTO
    public ListingResponse toResponse(FoodListing listing, boolean showExactAddress) {
        return ListingResponse.builder()
                .id(listing.getId())
                .title(listing.getTitle())
                .description(listing.getDescription())
                .servings(listing.getServings())
                .foodType(listing.getFoodType())
                .cookedAt(listing.getCookedAt())
                .pickupBy(listing.getPickupBy())
                .areaName(listing.getAreaName())
                .exactAddress(showExactAddress ? listing.getExactAddress() : "Address revealed after approval")
                .city(listing.getCity())
                .state(listing.getState())
                .pincode(listing.getPincode())
                .status(listing.getStatus())
                .createdAt(listing.getCreatedAt())
                .postedById(listing.getPostedBy().getId())
                .postedByName(listing.getPostedBy().getName())
                .postedByTrustScore(listing.getPostedBy().getTrustScore())
                .build();
    }

    // Get all available listings (no city filter)
    public List<ListingResponse> getAllAvailableListings() {
        return listingRepository
                .findByStatus(ListingStatus.AVAILABLE)
                .stream()
                .map(l -> toResponse(l, false))
                .collect(Collectors.toList());
    }
}
