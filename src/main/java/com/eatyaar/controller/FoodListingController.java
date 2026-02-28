package com.eatyaar.controller;

import com.eatyaar.dto.request.CreateListingRequest;
import com.eatyaar.dto.response.ListingResponse;
import com.eatyaar.entity.User;
import com.eatyaar.service.FoodListingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/listings")
@RequiredArgsConstructor
public class FoodListingController {

    private final FoodListingService listingService;

    // POST /api/listings — Create new listing (auth required)
    @PostMapping
    public ResponseEntity<ListingResponse> createListing(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CreateListingRequest request) {
        return ResponseEntity.ok(listingService.createListing(currentUser, request));
    }

    @GetMapping
    public ResponseEntity<List<ListingResponse>> getListings(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String area) {

        if (area != null) {
            return ResponseEntity.ok(listingService.getListingsByArea(area));
        }
        if (city != null) {
            return ResponseEntity.ok(listingService.getListingsByCity(city));
        }
        // No filter — return all available
        return ResponseEntity.ok(listingService.getAllAvailableListings());
    }

    @GetMapping("/all")
    public ResponseEntity<List<ListingResponse>> getAllListings() {
        return ResponseEntity.ok(listingService.getAllAvailableListings());
    }


    // GET /api/listings/my — My posted listings (auth required)
    @GetMapping("/my")
    public ResponseEntity<List<ListingResponse>> getMyListings(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(listingService.getMyListings(currentUser));
    }

    // GET /api/listings/{id} — Get one listing (public)
    @GetMapping("/{id}")
    public ResponseEntity<ListingResponse> getListingById(@PathVariable Long id) {
        return ResponseEntity.ok(listingService.getListingById(id, false));
    }

    // PATCH /api/listings/{id}/expire — Mark as expired (owner only)
    @PatchMapping("/{id}/expire")
    public ResponseEntity<ListingResponse> expireListing(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(listingService.expireListing(id, currentUser));
    }

    // DELETE /api/listings/{id} — Delete listing (owner only)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteListing(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        listingService.deleteListing(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
