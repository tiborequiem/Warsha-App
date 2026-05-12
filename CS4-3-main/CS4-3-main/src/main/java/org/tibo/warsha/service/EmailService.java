package org.tibo.warsha.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

//@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ── Registration confirmed ────────────────────────────────────────────────

    public void sendRegistrationConfirmation(String toEmail, String username) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(toEmail);
        msg.setSubject("Welcome to Warsha, " + username + "!");
        msg.setText(
                "Hi " + username + ",\n\n" +
                        "Your Warsha account has been created successfully.\n\n" +
                        "You can now log in and start booking local services.\n\n" +
                        "— The Warsha Team"
        );
        mailSender.send(msg);
    }

    // ── Appointment accepted ──────────────────────────────────────────────────

    public void sendAppointmentConfirmed(String toEmail, String customerName,
                                         String workerName, String serviceType,
                                         String appointmentDate) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(toEmail);
        msg.setSubject("Appointment Confirmed – Warsha");
        msg.setText(
                "Hi " + customerName + ",\n\n" +
                        "Great news! Your appointment has been confirmed.\n\n" +
                        "  Service  : " + serviceType + "\n" +
                        "  Worker   : " + workerName + "\n" +
                        "  Date/Time: " + appointmentDate + "\n\n" +
                        "If you need to cancel, please do so from your appointments page.\n\n" +
                        "— The Warsha Team"
        );
        mailSender.send(msg);
    }

    // ── Appointment rejected / cancelled ─────────────────────────────────────

    public void sendAppointmentCancelled(String toEmail, String recipientName,
                                         String serviceType, String appointmentDate) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(toEmail);
        msg.setSubject("Appointment Cancelled – Warsha");
        msg.setText(
                "Hi " + recipientName + ",\n\n" +
                        "Your appointment has been cancelled.\n\n" +
                        "  Service  : " + serviceType + "\n" +
                        "  Date/Time: " + appointmentDate + "\n\n" +
                        "You can book a new appointment any time from the Warsha website.\n\n" +
                        "— The Warsha Team"
        );
        mailSender.send(msg);
    }
}

