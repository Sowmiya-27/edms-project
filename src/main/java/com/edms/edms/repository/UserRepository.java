package com.edms.edms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.edms.edms.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
