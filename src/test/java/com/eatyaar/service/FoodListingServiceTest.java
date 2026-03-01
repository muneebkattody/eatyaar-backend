// ── FoodListingServiceTest.java ──────────────────────────────────
package com.eatyaar.service;

import com.eatyaar.dto.request.CreateListingRequest;
import com.eatyaar.dto.response.ListingResponse;
import com.eatyaar.entity.FoodListing;
import com.eatyaar.entity.FoodListing.FoodType;
import com.eatyaar.entity.FoodListing.ListingStatus;
import com.eatyaar.entity.User;
import com.eatyaar.repository.FoodListingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FoodListingService Tests")
class FoodListingServiceTest {

    @Mock private FoodListingRepository listingRepository;
    @InjectMocks private FoodListingService listingService;

    private User owner;
    private User otherUser;
    private FoodListing listing;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .id(1L).name("Owner").phone("9000000001")
                .trustScore(4.5).totalGiven(3).totalTaken(1).build();

        otherUser = User.builder()
                .id(2L).name("Other").phone("9000000002").build();

        listing = FoodListing.builder()
                .id(10L).postedBy(owner)
                .title("Dal Tadka").description("Freshly made")
                .servings(4).foodType(FoodType.VEG)
                .areaName("Aundh").exactAddress("Flat 2A, Blue Tower")
                .city("Pune").state("Maharashtra").pincode("411007")
                .status(ListingStatus.AVAILABLE)
                .cookedAt(LocalDateTime.now().minusHours(1))
                .pickupBy(LocalDateTime.now().plusHours(4))
                .build();
    }

    @Test
    @DisplayName("createListing: creates and returns listing")
    void createListing_success() {
        CreateListingRequest request = new CreateListingRequest();
        request.setTitle("Dal Tadka");
        request.setServings(4);
        request.setFoodType(FoodType.VEG);
        request.setAreaName("Aundh");
        request.setExactAddress("Flat 2A, Blue Tower");
        request.setCity("Pune");
        request.setState("Maharashtra");
        request.setPincode("411007");
        request.setCookedAt(LocalDateTime.now().minusHours(1));
        request.setPickupBy(LocalDateTime.now().plusHours(4));

        when(listingRepository.save(any())).thenAnswer(inv -> {
            FoodListing l = inv.getArgument(0);
            l.setId(10L);
            return l;
        });

        ListingResponse response = listingService.createListing(owner, request);

        assertThat(response.getTitle()).isEqualTo("Dal Tadka");
        assertThat(response.getPostedById()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(ListingStatus.AVAILABLE);
        // Owner sees exact address
        assertThat(response.getExactAddress()).isEqualTo("Flat 2A, Blue Tower");
    }

    @Test
    @DisplayName("getListingsByCity: returns only AVAILABLE listings")
    void getListingsByCity_returnsAvailable() {
        when(listingRepository.findByCityAndStatus("Pune", ListingStatus.AVAILABLE))
                .thenReturn(List.of(listing));

        List<ListingResponse> results = listingService.getListingsByCity("Pune");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Dal Tadka");
        // Public — exact address hidden
        assertThat(results.get(0).getExactAddress()).doesNotContain("Flat 2A");
    }

    @Test
    @DisplayName("expireListing: owner can expire listing")
    void expireListing_ownerSuccess() {
        when(listingRepository.findById(10L)).thenReturn(Optional.of(listing));
        when(listingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ListingResponse response = listingService.expireListing(10L, owner);

        assertThat(response.getStatus()).isEqualTo(ListingStatus.EXPIRED);
    }

    @Test
    @DisplayName("expireListing: non-owner cannot expire listing")
    void expireListing_nonOwner_throws() {
        when(listingRepository.findById(10L)).thenReturn(Optional.of(listing));

        assertThatThrownBy(() -> listingService.expireListing(10L, otherUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not the owner");
    }

    @Test
    @DisplayName("deleteListing: owner can delete listing")
    void deleteListing_ownerSuccess() {
        when(listingRepository.findById(10L)).thenReturn(Optional.of(listing));

        assertThatNoException().isThrownBy(() ->
                listingService.deleteListing(10L, owner)
        );

        verify(listingRepository).delete(listing);
    }

    @Test
    @DisplayName("deleteListing: non-owner cannot delete")
    void deleteListing_nonOwner_throws() {
        when(listingRepository.findById(10L)).thenReturn(Optional.of(listing));

        assertThatThrownBy(() -> listingService.deleteListing(10L, otherUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not the owner");

        verify(listingRepository, never()).delete(any());
    }

    @Test
    @DisplayName("toResponse: hides exact address for public view")
    void toResponse_hidesAddressPublicly() {
        ListingResponse response = listingService.toResponse(listing, false);
        assertThat(response.getExactAddress()).contains("revealed after approval");
    }

    @Test
    @DisplayName("toResponse: shows exact address for owner/approved taker")
    void toResponse_showsAddressWhenAllowed() {
        ListingResponse response = listingService.toResponse(listing, true);
        assertThat(response.getExactAddress()).isEqualTo("Flat 2A, Blue Tower");
    }
}