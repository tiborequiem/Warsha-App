package org.tibo.warsha.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Data;
@Data
@Entity
@Table(name = "users")

public class User {

    public User() {

    }

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

    @NotBlank(message = "Username is required") /*We validate each input as we did in web. I found these annotations which make things 10x easier */
    @Size(min = 3, max = 30, message = "Username must be 3–30 characters")
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




    // worker related stuff down there yo

    private String trade;

    private String serviceArea;

    private Integer yearsExperience;

    @Size(max = 20)
    private String phone;

}
