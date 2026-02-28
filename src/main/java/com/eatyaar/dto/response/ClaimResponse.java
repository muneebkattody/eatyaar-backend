package com.eatyaar.dto.response;

import com.eatyaar.entity.Claim.ClaimStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ClaimResponse {

    private Long id;
    private ClaimStatus status;
    private LocalDateTime createdAt;

    // Listing info
    private Long listingId;
    private String listingTitle;
    private String listingAreaName;
    private String exactAddress; // only shown when claim is APPROVED

    // Claimer info
    private Long claimedById;
    private String claimedByName;
}