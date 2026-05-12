package org.tibo.warsha.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;        // ← ADDED: for price field
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "appointments")
public class Appointment {

    public Appointment() {}

    // ← UPDATED: changed CONFIRMED to ACCEPTED, added REJECTED
    public enum Status {
        PENDING, ACCEPTED, REJECTED, COMPLETED, CANCELLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Customer is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @NotNull(message = "Worker is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    private User worker;

    @NotBlank(message = "Service type is required")
    @Column(nullable = false)
    private String serviceType;

    @NotNull(message = "Appointment date is required")
    @Column(nullable = false)
    private LocalDateTime appointmentDate;

    @Column(length = 500)
    private String notes;

    // ← ADDED: price for earnings calculation
    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}