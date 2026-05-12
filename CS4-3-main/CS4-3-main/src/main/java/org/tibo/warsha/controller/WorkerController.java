package org.tibo.warsha.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.tibo.warsha.model.User;
import org.tibo.warsha.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class WorkerController {

    private final UserService userService;

    public WorkerController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/workers")
    public String listWorkers(@RequestParam(required = false) String trade,
                              @RequestParam(required = false) String serviceArea,
                              @AuthenticationPrincipal UserDetails principal,
                              Model model) {
        List<User> workers;
        if (trade != null && !trade.isBlank() && serviceArea != null && !serviceArea.isBlank()) {
            workers = userService.findWorkersByTradeAndServiceArea(trade.trim(), serviceArea.trim());
        } else if (trade != null && !trade.isBlank()) {
            workers = userService.findWorkersByTrade(trade.trim());
        } else if (serviceArea != null && !serviceArea.isBlank()) {
            workers = userService.findWorkersByServiceArea(serviceArea.trim());
        } else {
            workers = userService.findAllWorkers();
        }
        model.addAttribute("workers", workers);
        model.addAttribute("tradeFilter", trade);
        model.addAttribute("areaFilter", serviceArea);

        if (principal != null) {
            userService.findByUsername(principal.getUsername())
                    .ifPresent(user -> model.addAttribute("user", user));
        }
        return "workers";
    }

    @GetMapping("/workers/{id}")
    public String showWorkerProfile(@PathVariable Long id,
                                    @AuthenticationPrincipal UserDetails principal,
                                    Model model) {
        User worker = userService.findById(id)
                .filter(u -> u.getRole() == User.Role.WORKER)
                .orElseThrow(() -> new IllegalArgumentException("Worker not found."));
        model.addAttribute("worker", worker);

        if (principal != null) {
            userService.findByUsername(principal.getUsername())
                    .ifPresent(user -> model.addAttribute("user", user));
        }
        return "worker-profile";
    }
}