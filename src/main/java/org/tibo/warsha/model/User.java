package org.tibo.warsha.model;

import lombok.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "users")
public class User {

    public User() {}

    public User(String username, String email, String password, Role role) {
        this.username = username;
        this.email    = email;
        this.password = password;
        this.role     = role;
    }

    public enum Role {
        USER, WORKER
    }



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 30, message = "Username must be 3-30 characters")
    @Column(nullable = false, unique = true)
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email address")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Password is required")
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    @Size(max = 100)
    private String fullName;

    @Size(max = 300)
    private String bio;

    // Worker fields
    private String trade;
    private String serviceArea;
    private Integer yearsExperience;

    @Size(max = 20)
    private String phone;

    // -- Worker profile fields ------------------------------------------------

    /** Base price charged per appointment. Used by PaymentService on completion. */
    @Column(precision = 10, scale = 2)
    private BigDecimal basePrice;

    /** Running average of all Review ratings for this worker. Recalculated by ReviewService. */
    @Column
    private Double averageRating;

    /** Cumulative earnings from all COMPLETED appointments. Updated by PaymentService. */
    @Column(precision = 12, scale = 2)
    private BigDecimal totalEarnings = BigDecimal.ZERO;
}
