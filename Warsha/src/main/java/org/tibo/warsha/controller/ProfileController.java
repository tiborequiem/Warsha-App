package com.example.warsha.controller;

import com.example.warsha.model.User;
import com.example.warsha.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String showProfile(@AuthenticationPrincipal UserDetails principal, Model model) {
        return userService.findByUsername(principal.getUsername())
                .map(user -> { model.addAttribute("user", user); return "profile"; })
                .orElse("redirect:/login");
    }

    @PostMapping("/update")
    public String updateProfile(@AuthenticationPrincipal UserDetails principal,
                                @RequestParam(required = false) String fullName,
                                @RequestParam(required = false) String bio,
                                @RequestParam(required = false) String email,
                                @RequestParam(required = false) String newPassword,
                                @RequestParam(required = false) String confirmPassword,
                                // worker fields
                                @RequestParam(required = false) String trade,
                                @RequestParam(required = false) String serviceArea,
                                @RequestParam(required = false) Integer yearsExperience,
                                @RequestParam(required = false) String phone,
                                RedirectAttributes ra) {

        User user = userService.findByUsername(principal.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        if (newPassword != null && !newPassword.isBlank()) {
            if (!newPassword.equals(confirmPassword)) {
                ra.addFlashAttribute("error", "New passwords do not match.");
                return "redirect:/profile";
            }
            if (newPassword.length() < 6) {
                ra.addFlashAttribute("error", "Password must be at least 6 characters.");
                return "redirect:/profile";
            }
        }

        try {
            userService.updateProfile(user.getId(), fullName, bio, email, newPassword,
                    trade, serviceArea, yearsExperience, phone);
            ra.addFlashAttribute("success", "Profile updated successfully.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/profile";
    }
}
