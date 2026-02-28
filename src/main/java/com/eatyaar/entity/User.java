package com.eatyaar.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String phone;

    @Column
    private String email;

    @Column
    private String city;

    @Column
    private String area;

    @Column(nullable = false)
    @Builder.Default
    private Double trustScore = 0.0;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalGiven = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalTaken = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

}
