package org.tibo.warsha.service;

import org.tibo.warsha.model.User;
import org.tibo.warsha.model.User.Role;
import org.tibo.warsha.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    public User register(String username, String email, String rawPassword) {
        checkUnique(username, email);
        User user = new User(username, email, passwordEncoder.encode(rawPassword), Role.USER);
        return userRepository.save(user);
    }


    public User registerWorker(String username,
                                        String email,
                                     String rawPassword,
                                      String trade,
                                    String serviceArea,
                                        Integer yearsExperience,
                               String phone) {

        checkUnique(username, email);
        User user = new User(username, email, passwordEncoder.encode(rawPassword), Role.WORKER);
        user.setTrade(trade);
        user.setServiceArea(serviceArea);
        user.setYearsExperience(yearsExperience);
        user.setPhone(phone);
        return userRepository.save(user);
    }


    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }



    private void checkUnique(String username, String email) {
        if (userRepository.existsByUsername(username))
            throw new IllegalArgumentException("Username already taken.");
        if (userRepository.existsByEmail(email))
            throw new IllegalArgumentException("Email already registered.");
    }
}
