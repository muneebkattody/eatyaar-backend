package com.eatyaar.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClaimRequest {

    @NotNull(message = "Listing ID is required")
    private Long listingId;
}