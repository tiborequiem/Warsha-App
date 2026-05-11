package org.tibo.warsha.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tibo.warsha.model.Appointment;
import org.tibo.warsha.model.Review;
import org.tibo.warsha.model.User;
import org.tibo.warsha.repository.AppointmentRepository;
import org.tibo.warsha.repository.ReviewRepository;
import org.tibo.warsha.repository.UserRepository;
import java.util.Optional;

// Handles review submission and keeps the worker's average rating up to date
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         AppointmentRepository appointmentRepository,
                         UserRepository userRepository) {
        this.reviewRepository      = reviewRepository;
        this.appointmentRepository = appointmentRepository;
        this.userRepository        = userRepository;
    }

    // Returns the review for an appointment if one has been submitted
    @Transactional(readOnly = true)
    public Optional<Review> findByAppointmentId(Long appointmentId) {
        return reviewRepository.findByAppointmentId(appointmentId);
    }

    // Returns true if a review already exists for this appointment
    @Transactional(readOnly = true)
    public boolean hasReview(Long appointmentId) {
        return reviewRepository.existsByAppointmentId(appointmentId);
    }

    // Validates, saves the review, then recalculates the worker's average rating
    @Transactional
    public Review submitReview(Long appointmentId, Long customerId, int rating, String comment) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found."));

        // Only completed appointments can be reviewed
        if (appointment.getStatus() != Appointment.Status.COMPLETED) {
            throw new IllegalArgumentException("Reviews can only be left for completed appointments.");
        }

        // Prevent submitting a second review for the same appointment
        if (reviewRepository.existsByAppointmentId(appointmentId)) {
            throw new IllegalArgumentException("A review has already been submitted for this appointment.");
        }

        // Ensure the review belongs to the correct customer
        if (!appointment.getCustomer().getId().equals(customerId)) {
            throw new IllegalArgumentException("You are not authorised to review this appointment.");
        }

        Review review = new Review();
        review.setAppointment(appointment);
        review.setRating(rating);
        review.setComment(comment);

        Review saved = reviewRepository.save(review);

        // Update the worker's average rating now that a new review exists
        recalculateAverageRating(appointment.getWorker());

        return saved;
    }

    // Runs AVG query across all reviews for the worker and saves the result
    private void recalculateAverageRating(User worker) {
        Double avg = reviewRepository.calculateAverageRatingForWorker(worker);
        worker.setAverageRating(avg);
        userRepository.save(worker);
    }
}
