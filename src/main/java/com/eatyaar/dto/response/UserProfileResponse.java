package com.eatyaar.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileResponse {

    private Long id;
    private String name;
    private String city;
    private String area;
    private Double trustScore;
    private Integer totalGiven;
    private Integer totalTaken;
}
