package com.eatyaar.dto.response;

import com.eatyaar.entity.FoodListing.FoodType;
import com.eatyaar.entity.FoodListing.ListingStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ListingResponse {

    private Long id;
    private String title;
    private String description;
    private Integer servings;
    private FoodType foodType;
    private LocalDateTime cookedAt;
    private LocalDateTime pickupBy;
    private String areaName;       // always visible
    private String exactAddress;   // only visible if claim is approved
    private String city;
    private String state;
    private String pincode;
    private ListingStatus status;
    private LocalDateTime createdAt;

    // Poster info (limited — don't expose full user object)
    private Long postedById;
    private String postedByName;
    private Double postedByTrustScore;
}
