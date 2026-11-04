package com.likehub.likehub.users;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByEmail(String email);
  Optional<User> findByUsername(String username);
  Optional<User> findByPublicId(UUID publicId);

  @EntityGraph(attributePaths = {"followers", "following"})
  @Query("SELECT u FROM User u WHERE u.publicId = :publicId")
  Optional<User> findByPublicIdWithFollowersAndFollowing(@Param("publicId") UUID publicId);

  boolean existsByEmail(String email);
  boolean existsByUsername(String username);
  boolean existsByDisplayName(String displayName);

  @Query("SELECT u FROM User u LEFT JOIN FETCH u.followers WHERE u.publicId = :publicId")
  Optional<User> findByPublicIdWithFollowers(@Param("publicId") UUID publicId);


  @Query("SELECT u FROM User u LEFT JOIN FETCH u.followers LEFT JOIN FETCH u.following WHERE u.username = :username")
  Optional<User> findByUsernameWithFollowNetwork(@Param("username") String username);
}
