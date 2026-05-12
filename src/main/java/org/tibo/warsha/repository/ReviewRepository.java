package org.tibo.warsha.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.tibo.warsha.model.Review;
import org.tibo.warsha.model.User;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Fetch the review for a specific appointment, if one exists
    Optional<Review> findByAppointmentId(Long appointmentId);

    // Quick check used to prevent duplicate reviews
    boolean existsByAppointmentId(Long appointmentId);

    // Calculates the worker's average rating across all their reviewed appointments
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.appointment.worker = :worker")
    Double calculateAverageRatingForWorker(@Param("worker") User worker);
}
