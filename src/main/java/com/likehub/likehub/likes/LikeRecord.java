package com.likehub.likehub.likes;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Getter
@Setter
@Table(name = "like_record",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_like_user_target",
                columnNames = {"user_id", "post_id", "target_type"}),
        indexes = @Index(name = "idx_like_post", columnList = "post_id"))
public class LikeRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "post_id", nullable = false, updatable = false)
    private UUID postId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,updatable = false)
    private TargetType targetType;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
    }
}