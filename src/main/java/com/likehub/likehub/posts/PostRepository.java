package com.likehub.likehub.posts;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, Long> {
   Optional<Post> findByPublicId(UUID publicId);
   @Query("SELECT DISTINCT p FROM Post p " +
           "LEFT JOIN FETCH p.hashtags " +
           "LEFT JOIN FETCH p.images " +
           "LEFT JOIN FETCH p.sender")
   Slice<Post> findFeedCandidates(Pageable pageable);
}
