package com.likehub.likehub.images;

import com.likehub.likehub.users.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter @Setter
@Table(
        name = "uploaded_images",
        indexes = {
                @Index(name = "idx_uploaded_owner", columnList = "owner_id"),
                @Index(name = "idx_uploaded_status_created", columnList = "status, created_at")
        }
)
public class UploadedImage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "owner_id",
            referencedColumnName = "public_id",
            nullable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_uploaded_images_owner"))
    private User owner;

    @Column(nullable = false, length = 2048, updatable = false)
    private String url;

    @Column(name = "storage_key", nullable = false, length = 512, updatable = false)
    private String storageKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ImageStatus status = ImageStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        if (status == null) status = ImageStatus.PENDING;
    }
}