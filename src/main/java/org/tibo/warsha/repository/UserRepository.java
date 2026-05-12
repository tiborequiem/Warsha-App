package org.tibo.warsha.repository;

import org.tibo.warsha.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    List<User> findByRole(User.Role role);

    List<User> findByRoleAndTradeContainingIgnoreCase(User.Role role, String trade);

    List<User> findByRoleAndServiceAreaContainingIgnoreCase(User.Role role, String serviceArea);

    List<User> findByRoleAndTradeContainingIgnoreCaseAndServiceAreaContainingIgnoreCase(
            User.Role role, String trade, String serviceArea);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}