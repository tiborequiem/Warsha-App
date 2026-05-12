package org.tibo.warsha.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.tibo.warsha.model.Payment;
import org.tibo.warsha.model.User;
import org.tibo.warsha.service.PaymentService;
import org.tibo.warsha.service.UserService;
import java.util.List;

// Serves the customer's payment history page at GET /customer/payments
@Controller
@RequestMapping("/customer/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final UserService userService;

    public PaymentController(PaymentService paymentService, UserService userService) {
        this.paymentService = paymentService;
        this.userService    = userService;
    }

    // Loads all payments for the logged-in customer and passes them to the template
    @GetMapping
    public String paymentHistory(@AuthenticationPrincipal UserDetails principal, Model model) {

        User customer = userService.findByUsername(principal.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found."));

        List<Payment> payments = paymentService.getPaymentHistoryForCustomer(customer);

        model.addAttribute("payments", payments);
        model.addAttribute("customer", customer);

        return "payment-history";
    }
}
