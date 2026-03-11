package com.eatyaar.dto.response;

//@Data
//@AllArgsConstructor
//public class AuthResponse {
//    private String token;
//    private Long userId;
//    private String phone;
//    private boolean isNewUser; // frontend uses this to show profile setup screen
//}


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private Long userId;
    private String email;   // was phone
    private Boolean isNewUser;
}