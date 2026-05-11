package org.tibo.warsha.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.tibo.warsha.model.Appointment;
import org.tibo.warsha.model.User;
import org.tibo.warsha.service.AppointmentService;
import org.tibo.warsha.service.ReviewService;
import org.tibo.warsha.service.UserService;

// Handles GET and POST for /customer/review/{appointmentId}
@Controller
@RequestMapping("/customer/review")
public class ReviewController {

    private final ReviewService reviewService;
    private final AppointmentService appointmentService;
    private final UserService userService;

    public ReviewController(ReviewService reviewService,
                            AppointmentService appointmentService,
                            UserService userService) {
        this.reviewService      = reviewService;
        this.appointmentService = appointmentService;
        this.userService        = userService;
    }

    // Shows the review form — only if the appointment is completed and has no review yet
    @GetMapping("/{appointmentId}")
    public String showReviewForm(@PathVariable Long appointmentId,
                                 @AuthenticationPrincipal UserDetails principal,
                                 Model model,
                                 RedirectAttributes ra) {

        User customer = resolveCustomer(principal);
        Appointment appointment = resolveAppointment(appointmentId, customer, ra);
        if (appointment == null) return "redirect:/appointments";

        model.addAttribute("appointment", appointment);
        return "review-form";
    }

    // Validates the form input, then saves the review via ReviewService
    @PostMapping("/{appointmentId}")
    public String submitReview(@PathVariable Long appointmentId,
                               @RequestParam(required = false) Integer rating,
                               @RequestParam(required = false) String comment,
                               @AuthenticationPrincipal UserDetails principal,
                               RedirectAttributes ra) {

        // Validate rating and comment before calling the service
        if (rating == null || rating < 1 || rating > 5) {
            ra.addFlashAttribute("error", "Please select a rating between 1 and 5.");
            return "redirect:/customer/review/" + appointmentId;
        }
        if (comment == null || comment.isBlank()) {
            ra.addFlashAttribute("error", "Please enter a comment.");
            return "redirect:/customer/review/" + appointmentId;
        }

        User customer = resolveCustomer(principal);

        try {
            reviewService.submitReview(appointmentId, customer.getId(), rating, comment.trim());
            ra.addFlashAttribute("success", "Thank you! Your review has been submitted.");
            return "redirect:/appointments";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/customer/review/" + appointmentId;
        }
    }

    // Loads the authenticated user from the security context
    private User resolveCustomer(UserDetails principal) {
        return userService.findByUsername(principal.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found."));
    }

    // Runs all defensive checks on the appointment — returns null and sets an error if any fail
    private Appointment resolveAppointment(Long appointmentId, User customer, RedirectAttributes ra) {

        Appointment appointment = appointmentService.findById(appointmentId).orElse(null);
        if (appointment == null) {
            ra.addFlashAttribute("error", "Appointment not found.");
            return null;
        }

        // Make sure this appointment belongs to the logged-in customer
        if (!appointment.getCustomer().getId().equals(customer.getId())) {
            ra.addFlashAttribute("error", "You are not authorised to review this appointment.");
            return null;
        }

        // Only completed appointments can be reviewed
        if (appointment.getStatus() != Appointment.Status.COMPLETED) {
            ra.addFlashAttribute("error", "Reviews can only be submitted for completed appointments.");
            return null;
        }

        // Block a second review for the same appointment
        if (reviewService.hasReview(appointmentId)) {
            ra.addFlashAttribute("error", "You have already submitted a review for this appointment.");
            return null;
        }

        return appointment;
    }
}
