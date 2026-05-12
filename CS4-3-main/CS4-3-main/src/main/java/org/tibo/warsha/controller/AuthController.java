package org.tibo.warsha.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.tibo.warsha.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // ── Root ──────────────────────────────────────────────────────────────────
    // After login, Spring sends everyone here. We route by role.

    @GetMapping("/")
    public String root(@AuthenticationPrincipal UserDetails principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        boolean isWorker = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_WORKER"));
        return isWorker ? "redirect:/worker/dashboard" : "redirect:/layout";
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @GetMapping("/login")
    public String showLogin(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            @RequestParam(required = false) String registered,
                            Model model) {
        if (error      != null) model.addAttribute("error", "Invalid username or password.");
        if (logout     != null) model.addAttribute("info",  "You have been logged out.");
        if (registered != null) model.addAttribute("info",  "Account created! Please log in.");
        return "login";
    }

    // ── Register (regular user) ───────────────────────────────────────────────

    @GetMapping("/register")
    public String showRegister() { return "register"; }

    @PostMapping("/register")
    public String doRegister(@RequestParam String username,
                             @RequestParam String email,
                             @RequestParam String password,
                             @RequestParam String confirmPassword,
                             Model model) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            return "register";
        }
        if (password.length() < 6) {
            model.addAttribute("error", "Password must be at least 6 characters.");
            return "register";
        }
        try {
            userService.register(username, email, password);
            return "redirect:/login?registered";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    // ── Register (worker) ─────────────────────────────────────────────────────

    @GetMapping("/register/worker")
    public String showWorkerRegister() { return "register-worker"; }

    @PostMapping("/register/worker")
    public String doWorkerRegister(@RequestParam String username,
                                   @RequestParam String email,
                                   @RequestParam String password,
                                   @RequestParam String confirmPassword,
                                   @RequestParam String trade,
                                   @RequestParam String serviceArea,
                                   @RequestParam(required = false) Integer yearsExperience,
                                   @RequestParam String phone,
                                   Model model) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            return "register-worker";
        }
        if (password.length() < 6) {
            model.addAttribute("error", "Password must be at least 6 characters.");
            return "register-worker";
        }
        try {
            userService.registerWorker(username, email, password,
                    trade, serviceArea, yearsExperience, phone);
            return "redirect:/login?registered";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "register-worker";
        }
    }
}