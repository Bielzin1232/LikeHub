package com.likehub.likehub.likes;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface LikeRecordRepository extends JpaRepository<LikeRecord, Long> {

    @Query("SELECT l FROM LikeRecord l WHERE l.user.publicId = :userId AND l.targetId = :targetId AND l.targetType = :targetType")
    Optional<LikeRecord> findByUserIdAndTargetIdAndTargetType(
            @Param("userId") UUID userId,
            @Param("targetId") UUID targetId,
            @Param("targetType") TargetType targetType
    );

    @Modifying
    @Query("DELETE FROM LikeRecord l WHERE l.user.publicId = :userId AND l.targetId = :targetId AND l.targetType = :targetType")
    void deleteByUserIdAndTargetIdAndTargetType(
            @Param("userId") UUID userId,
            @Param("targetId") UUID targetId,
            @Param("targetType") TargetType targetType
    );

    @Query("SELECT COUNT(l) FROM LikeRecord l WHERE l.targetId = :targetId AND l.targetType = :targetType")
    long countByTargetIdAndTargetType(@Param("targetId") UUID targetId, @Param("targetType") TargetType targetType);

    @Query(
            "SELECT l.user.username FROM LikeRecord l WHERE l.targetId = :targetId AND l.targetType = 'POST'"
    )
    Slice<String> findUsernamesByTargetId(
            @Param("targetId") UUID targetId,
            Pageable pageable
    );
}
