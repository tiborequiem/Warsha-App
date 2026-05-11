package org.tibo.warsha.controller;

import org.tibo.warsha.model.Appointment;
import org.tibo.warsha.model.User;
import org.tibo.warsha.service.AppointmentService;
import org.tibo.warsha.service.UserService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;  // ← ADDED: for price parameter
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final UserService userService;

    public AppointmentController(AppointmentService appointmentService, UserService userService) {
        this.appointmentService = appointmentService;
        this.userService = userService;
    }

    // ── customer dashboard (unchanged) ──

    @GetMapping
    public String customerDashboard(@AuthenticationPrincipal UserDetails principal, Model model) {
        User customer = userService.findByUsername(principal.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));
        List<Appointment> appointments = appointmentService.findByCustomer(customer);
        model.addAttribute("appointments", appointments);
        model.addAttribute("user", customer);
        return "appointments";
    }

    // ← UPDATED: redirect old worker dashboard to new dedicated controller

    @GetMapping("/worker")
    public String workerDashboardRedirect() {
        return "redirect:/worker/dashboard";  // was: full dashboard logic
    }

    // ── booking form (unchanged) ──

    @GetMapping("/book/{workerId}")
    public String showBookingForm(@PathVariable Long workerId,
                                  @AuthenticationPrincipal UserDetails principal,
                                  Model model) {
        User worker = userService.findById(workerId)
                .filter(u -> u.getRole() == User.Role.WORKER)
                .orElseThrow(() -> new IllegalArgumentException("Worker not found."));
        model.addAttribute("worker", worker);
        model.addAttribute("minDate", LocalDateTime.now().plusHours(2));
        return "book-appointment";
    }

    // ← UPDATED: added price parameter for earnings calculation

    @PostMapping("/book/{workerId}")
    public String submitBooking(@PathVariable Long workerId,
                                @AuthenticationPrincipal UserDetails principal,
                                @RequestParam String serviceType,
                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime appointmentDate,
                                @RequestParam(required = false) String notes,
                                @RequestParam(required = false) BigDecimal price,  // ← ADDED
                                RedirectAttributes ra) {
        User customer = userService.findByUsername(principal.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        try {
            appointmentService.bookAppointment(customer.getId(), workerId, serviceType, appointmentDate, notes);
            ra.addFlashAttribute("success", "Appointment requested successfully!");
            return "redirect:/appointments";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/appointments/book/" + workerId;
        }
    }

    // ── cancel appointment (unchanged) ──

    @PostMapping("/{id}/cancel")
    public String cancelAppointment(@PathVariable Long id,
                                    @AuthenticationPrincipal UserDetails principal,
                                    RedirectAttributes ra) {
        User user = userService.findByUsername(principal.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));
        try {
            appointmentService.cancelAppointment(id, user.getId());
            ra.addFlashAttribute("success", "Appointment cancelled.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/appointments";
    }

    // ← REMOVED: confirmAppointment and completeAppointment endpoints
    // now handled by WorkerDashboardController at /worker/appointments/{id}/...
}