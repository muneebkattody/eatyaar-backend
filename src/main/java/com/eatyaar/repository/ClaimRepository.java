package com.eatyaar.repository;

import com.eatyaar.entity.Claim;
import com.eatyaar.entity.FoodListing;
import com.eatyaar.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {
    // Get all claims made by a user
    List<Claim> findByClaimedBy(User claimedBy);

    // Get all claims on a specific listing
    List<Claim> findByListing(FoodListing listing);

    // Check if user already claimed a listing (prevent duplicate claims)
    boolean existsByListingAndClaimedBy(FoodListing listing, User claimedBy);
}