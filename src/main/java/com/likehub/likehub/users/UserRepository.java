package com.likehub.likehub.users;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByEmail(String email);
  Optional<User> findByUsername(String username);
  Optional<User> findByPublicId(java.util.UUID publicId);
  boolean existsByEmail(String email);
  boolean existsByUsername(String username);
}