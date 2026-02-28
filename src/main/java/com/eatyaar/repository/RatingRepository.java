package com.eatyaar.repository;

import com.eatyaar.entity.Rating;
import com.eatyaar.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    // Get all ratings received by a user
    List<Rating> findByGivenTo(User givenTo);

    // Calculate average trust score for a user
    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.givenTo = :user")
    Double calculateAverageScore(User user);

    // Check if a rating already exists for this listing by this user (prevent duplicates)
    boolean existsByListingAndGivenBy(com.eatyaar.entity.FoodListing listing, User givenBy);
}
