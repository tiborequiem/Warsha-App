package org.tibo.warsha.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.tibo.warsha.model.User;
import org.tibo.warsha.service.AppointmentService;
import org.tibo.warsha.service.UserService;

import java.util.List;

@Controller
public class HomeController {

    private final UserService userService;
    private final AppointmentService appointmentService;

    public HomeController(UserService userService, AppointmentService appointmentService) {
        this.userService = userService;
        this.appointmentService = appointmentService;
    }

    // Customer home page — quick links + upcoming appointments preview
    @GetMapping("/layout")
    public String customerHome(@AuthenticationPrincipal UserDetails principal, Model model) {
        User customer = userService.findByUsername(principal.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        List<User> workers = userService.findAllWorkers();
        var appointments = appointmentService.findByCustomer(customer);

        long pendingCount = appointments.stream()
                .filter(a -> a.getStatus() == org.tibo.warsha.model.Appointment.Status.PENDING)
                .count();
        long confirmedCount = appointments.stream()
                .filter(a -> a.getStatus() == org.tibo.warsha.model.Appointment.Status.CONFIRMED)
                .count();

        model.addAttribute("user", customer);
        model.addAttribute("workers", workers);
        model.addAttribute("appointments", appointments.stream().limit(5).toList());
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("confirmedCount", confirmedCount);
        model.addAttribute("totalAppointments", appointments.size());

        return "layout";
    }
}