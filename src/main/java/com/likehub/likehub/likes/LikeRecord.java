package com.likehub.likehub.likes;

import java.time.Instant;
import java.util.UUID;
import com.likehub.likehub.users.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "like_records",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_like_user_target",
                columnNames = {"user_id", "target_id", "target_type"}),
        indexes = {
                @Index(name = "idx_like_target", columnList = "target_id"),
                @Index(name = "idx_like_user", columnList = "user_id")
        })
public class LikeRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_likes_user"))
    private User user;

    @Column(name = "target_id", nullable = false, updatable = false)
    private UUID targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, updatable = false)
    private TargetType targetType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
    }
}