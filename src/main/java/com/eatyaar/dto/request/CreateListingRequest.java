package com.eatyaar.dto.request;

import com.eatyaar.entity.FoodListing.FoodType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateListingRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description; // optional

    @NotNull(message = "Servings is required")
    @Min(value = 1, message = "Servings must be at least 1")
    private Integer servings;

    @NotNull(message = "Food type is required")
    private FoodType foodType;

    @NotNull(message = "Cooked time is required")
    private LocalDateTime cookedAt;

    @NotNull(message = "Pickup by time is required")
    @Future(message = "Pickup time must be in the future")
    private LocalDateTime pickupBy;

    @NotBlank(message = "Area name is required")
    private String areaName;

    @NotBlank(message = "Exact address is required")
    private String exactAddress;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Pincode is required")
    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Enter a valid 6-digit pincode")
    private String pincode;
}