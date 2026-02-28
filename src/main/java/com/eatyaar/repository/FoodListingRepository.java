package com.eatyaar.repository;

import com.eatyaar.entity.FoodListing;
import com.eatyaar.entity.FoodListing.ListingStatus;
import com.eatyaar.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FoodListingRepository extends JpaRepository<FoodListing, Long> {

    // Get all available listings in a city
    List<FoodListing> findByCityAndStatus(String city, ListingStatus status);

    // Get all available listings in an area
    List<FoodListing> findByAreaNameAndStatus(String areaName, ListingStatus status);

    // Get all listings posted by a specific user
    List<FoodListing> findByPostedBy(User postedBy);

    // Get ALL available listings across all cities
    List<FoodListing> findByStatus(ListingStatus status);
}
