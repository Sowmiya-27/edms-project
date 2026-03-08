package com.edms.edms.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.edms.edms.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    // Lookup by email (already present)
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

}
