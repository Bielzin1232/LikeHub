package com.likehub.likehub.images;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface UploadedImageRepository extends JpaRepository<UploadedImage, UUID> {

    List<UploadedImage> findByStatusAndCreatedAtBefore(ImageStatus status, Instant cutoff);

    @Modifying
    @Query("DELETE FROM UploadedImage u WHERE u.status = :status AND u.createdAt < :cutoff")
    int deleteStale(@Param("status") ImageStatus status, @Param("cutoff") Instant cutoff);
}