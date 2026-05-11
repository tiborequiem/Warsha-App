package org.tibo.warsha.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.tibo.warsha.model.Appointment;
import org.tibo.warsha.model.User;
import org.tibo.warsha.service.AppointmentService;
import org.tibo.warsha.service.UserService;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/worker")
@PreAuthorize("hasRole('WORKER')")
public class WorkerDashboardController {  // ← was WorkerDashboard (renamed for convention)

    private final AppointmentService appointmentService;
    private final UserService userService;

    public WorkerDashboardController(AppointmentService appointmentService, UserService userService) {
        this.appointmentService = appointmentService;
        this.userService = userService;
    }

    // ← dashboard view

    @GetMapping("/dashboard")
    public String workerDashboard(@AuthenticationPrincipal UserDetails principal, Model model) {
        User worker = getAuthenticatedWorker(principal);

        long pendingCount = appointmentService.getPendingRequestsCount(worker);
        List<Appointment> todaysAppointments = appointmentService.getTodaysAppointments(worker);
        BigDecimal totalEarnings = worker.getTotalEarnings() != null
                ? worker.getTotalEarnings()
                : BigDecimal.ZERO;
        List<Appointment> allAppointments = appointmentService.findByWorker(worker);

        model.addAttribute("worker", worker);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("todaysAppointments", todaysAppointments);
        model.addAttribute("todaysCount", todaysAppointments.size());
        model.addAttribute("totalEarnings", totalEarnings);
        model.addAttribute("allAppointments", allAppointments);

        return "worker/dashboard";
    }

    // ← accept appointment

    @PostMapping("/appointments/{id}/accept")
    public String acceptAppointment(@PathVariable Long id,
                                    @AuthenticationPrincipal UserDetails principal,
                                    RedirectAttributes ra) {
        User worker = getAuthenticatedWorker(principal);

        try {
            appointmentService.acceptAppointment(id, worker.getId());
            ra.addFlashAttribute("success", "Appointment accepted successfully.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/worker/dashboard";
    }

    // ← ADDED: reject appointment (was missing in your version)

    @PostMapping("/appointments/{id}/reject")
    public String rejectAppointment(@PathVariable Long id,
                                    @AuthenticationPrincipal UserDetails principal,
                                    RedirectAttributes ra) {
        User worker = getAuthenticatedWorker(principal);

        try {
            appointmentService.rejectAppointment(id, worker.getId());
            ra.addFlashAttribute("success", "Appointment rejected.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/worker/dashboard";
    }

    // ← complete appointment

    @PostMapping("/appointments/{id}/complete")
    public String completeAppointment(@PathVariable Long id,
                                      @AuthenticationPrincipal UserDetails principal,
                                      RedirectAttributes ra) {
        User worker = getAuthenticatedWorker(principal);

        try {
            appointmentService.completeAppointment(id, worker.getId());
            ra.addFlashAttribute("success", "Appointment marked as completed. Earnings updated.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/worker/dashboard";
    }

    // ← earnings history

    @GetMapping("/earnings")
    public String earningsHistory(@AuthenticationPrincipal UserDetails principal, Model model) {
        User worker = getAuthenticatedWorker(principal);

        List<Appointment> completedAppointments = appointmentService.getCompletedAppointments(worker);
        BigDecimal totalEarnings = worker.getTotalEarnings() != null
                ? worker.getTotalEarnings()
                : BigDecimal.ZERO;

        model.addAttribute("worker", worker);
        model.addAttribute("completedAppointments", completedAppointments);
        model.addAttribute("totalEarnings", totalEarnings);
        model.addAttribute("completedCount", completedAppointments.size());

        return "worker/earnings";
    }

    // ← helper: get authenticated worker from security context

    private User getAuthenticatedWorker(UserDetails principal) {
        return userService.findByUsername(principal.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated worker not found"));
    }
}