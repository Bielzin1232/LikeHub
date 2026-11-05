package com.likehub.likehub.comments;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

     List<Comment> findByPost_PublicIdOrderByCreatedAtDesc(UUID postPublicId);
    Optional<Comment> findByPost_PublicIdAndId(UUID postPublicId, UUID id);

    Page<Comment> findByPost_PublicIdOrderByCreatedAtDesc(
            UUID postPublicId,
            Pageable pageable
    );

    Page<Comment> findByPost_PublicIdAndParentIsNullOrderByCreatedAtDesc(
            UUID postPublicId,
            Pageable pageable
    );

    List<Comment> findByParent_IdOrderByCreatedAtAsc(UUID parentCommentId);
}
