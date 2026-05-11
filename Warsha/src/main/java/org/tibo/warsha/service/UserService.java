package org.tibo.warsha.service;

import org.tibo.warsha.model.User;
import org.tibo.warsha.model.User.Role;
import org.tibo.warsha.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ── registration ──

    @Transactional
    public User register(String username, String email, String rawPassword) {
        checkUnique(username, email);
        User user = new User(username, email, passwordEncoder.encode(rawPassword), Role.USER);
        return userRepository.save(user);
    }

    @Transactional
    public User registerWorker(String username, String email, String rawPassword,
                               String trade, String serviceArea, Integer yearsExperience, String phone) {
        checkUnique(username, email);
        User user = new User(username, email, passwordEncoder.encode(rawPassword), Role.WORKER);
        user.setTrade(trade);
        user.setServiceArea(serviceArea);
        user.setYearsExperience(yearsExperience);
        user.setPhone(phone);

        // ← ADDED: initialize earnings to zero for new workers
        user.setTotalEarnings(BigDecimal.ZERO);

        return userRepository.save(user);
    }

    // ── queries (unchanged) ──

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<User> findAllWorkers() {
        return userRepository.findByRole(Role.WORKER);
    }

    @Transactional(readOnly = true)
    public List<User> findWorkersByTrade(String trade) {
        return userRepository.findByRoleAndTradeContainingIgnoreCase(Role.WORKER, trade);
    }

    @Transactional(readOnly = true)
    public List<User> findWorkersByServiceArea(String serviceArea) {
        return userRepository.findByRoleAndServiceAreaContainingIgnoreCase(Role.WORKER, serviceArea);
    }

    @Transactional(readOnly = true)
    public List<User> findWorkersByTradeAndServiceArea(String trade, String serviceArea) {
        return userRepository.findByRoleAndTradeContainingIgnoreCaseAndServiceAreaContainingIgnoreCase(
                Role.WORKER, trade, serviceArea);
    }

    // ── profile update (unchanged) ──

    @Transactional
    public User updateProfile(Long userId, String fullName, String bio, String email,
                              String newPassword, String trade, String serviceArea,
                              Integer yearsExperience, String phone) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        if (fullName != null) user.setFullName(fullName);
        if (bio != null) user.setBio(bio);
        if (email != null && !email.equals(user.getEmail())) {
            if (userRepository.existsByEmail(email)) {
                throw new IllegalArgumentException("Email already registered.");
            }
            user.setEmail(email);
        }
        if (newPassword != null && !newPassword.isBlank()) {
            user.setPassword(passwordEncoder.encode(newPassword));
        }
        if (user.getRole() == Role.WORKER) {
            if (trade != null) user.setTrade(trade);
            if (serviceArea != null) user.setServiceArea(serviceArea);
            if (yearsExperience != null) user.setYearsExperience(yearsExperience);
            if (phone != null) user.setPhone(phone);
        }
        return userRepository.save(user);
    }

    // ← ADDED: simple save for earnings updates from AppointmentService

    @Transactional
    public void save(User user) {
        userRepository.save(user);
    }

    // ── helpers ──

    private void checkUnique(String username, String email) {
        if (userRepository.existsByUsername(username))
            throw new IllegalArgumentException("Username already taken.");
        if (userRepository.existsByEmail(email))
            throw new IllegalArgumentException("Email already registered.");
    }
}