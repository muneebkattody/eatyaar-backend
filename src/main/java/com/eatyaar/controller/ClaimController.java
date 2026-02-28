package com.eatyaar.controller;

import com.eatyaar.dto.request.ClaimRequest;
import com.eatyaar.dto.response.ClaimResponse;
import com.eatyaar.entity.User;
import com.eatyaar.service.ClaimService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimService claimService;

    // POST /api/claims — Taker claims a listing
    @PostMapping
    public ResponseEntity<ClaimResponse> claimListing(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ClaimRequest request) {
        return ResponseEntity.ok(claimService.claimListing(currentUser, request));
    }

    // GET /api/claims/my — Get my claims
    @GetMapping("/my")
    public ResponseEntity<List<ClaimResponse>> getMyClaims(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(claimService.getMyClaims(currentUser));
    }

    // PATCH /api/claims/{id}/approve — Giver approves
    @PatchMapping("/{id}/approve")
    public ResponseEntity<ClaimResponse> approveClaim(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(claimService.approveClaim(id, currentUser));
    }

    // PATCH /api/claims/{id}/reject — Giver rejects
    @PatchMapping("/{id}/reject")
    public ResponseEntity<ClaimResponse> rejectClaim(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(claimService.rejectClaim(id, currentUser));
    }

    // PATCH /api/claims/{id}/picked-up — Taker marks as picked up
    @PatchMapping("/{id}/picked-up")
    public ResponseEntity<ClaimResponse> markPickedUp(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(claimService.markPickedUp(id, currentUser));
    }
}
