package com.likehub.likehub.likes;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface LikeRecordRepository extends JpaRepository<LikeRecord, Long> {
    @Query("SELECT l FROM LikeRecord l WHERE l.userId = :userId AND l.postId = :postId AND l.targetType = :targetType")
    LikeRecord findByUserIdAndPostIdAndTargetType(@Param("userId") java.util.UUID userId, @Param("postId") java.util.UUID postId, @Param("targetType") TargetType targetType);

    long deleteByUserIdAndPostIdAndTargetType(UUID userId, UUID postId, TargetType targetType);

    long countByPostIdAndTargetType(UUID postId, TargetType targetType);
}
