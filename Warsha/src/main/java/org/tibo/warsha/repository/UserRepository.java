package org.tibo.warsha.repository;

import org.springframework.stereotype.Repository;
import org.tibo.warsha.model.User;

import java.util.*;

@Repository
public interface UserRepository {

    List<User> findAll();

    Optional<User> findById(Long id);

    User save(User user);

    void deleteById(Long id);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
