package org.tibo.warsha.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

// Payment record created automatically when an appointment is marked COMPLETED

@Entity
@Table(name = "payments")
public class Payment {

    // Supported payment methods — CASH is the default
    public enum PaymentMethod { CASH, CARD }

    // Only one status exists since payments are always created as completed
    public enum PaymentStatus { COMPLETED }

    public Payment() {}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // One payment per appointment — unique FK enforces this
    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private Appointment appointment;

    // Taken from the worker's basePrice at the time of completion
    @NotNull
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    // Defaults to CASH as required — can be extended to CARD later
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method = PaymentMethod.CASH;

    // Always COMPLETED for auto-generated records
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.COMPLETED;

    // Set automatically when the record is created
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
