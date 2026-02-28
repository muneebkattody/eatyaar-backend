package com.eatyaar.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class RatingResponse {

    private Long id;
    private Integer score;
    private String comment;
    private LocalDateTime createdAt;

    // Who gave the rating
    private Long givenById;
    private String givenByName;

    // Who received the rating
    private Long givenToId;
    private String givenToName;

    // Which listing this was for
    private Long listingId;
    private String listingTitle;
}
