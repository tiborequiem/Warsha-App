package org.tibo.warsha.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.tibo.warsha.service.EmailService;
import org.tibo.warsha.service.UserService;

@Controller
@RequestMapping("/dev/email")
public class DevEmailController {

    private final EmailService emailService;
    private final UserService userService;

    public DevEmailController(EmailService emailService, UserService userService) {
        this.emailService = emailService;
        this.userService = userService;
    }

    @GetMapping("/test")
    public String sendTestEmail(@AuthenticationPrincipal UserDetails principal,
                                RedirectAttributes ra) {
        var user = userService.findByUsername(principal.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        try {
            emailService.sendRegistrationConfirmation(user.getEmail(), user.getUsername());
            ra.addFlashAttribute("success", "Test email sent to " + user.getEmail());
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to send email: " + e.getMessage());
        }

        return "redirect:/profile";
    }

    @GetMapping("/test-async")
    public String sendTestEmailAsync(@AuthenticationPrincipal UserDetails principal,
                                     RedirectAttributes ra) {
        var user = userService.findByUsername(principal.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        try {
            emailService.sendRegistrationConfirmationAsync(user.getEmail(), user.getUsername());
            ra.addFlashAttribute("success", "Test email queued for " + user.getEmail());
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to queue email: " + e.getMessage());
        }

        return "redirect:/profile";
    }
}

