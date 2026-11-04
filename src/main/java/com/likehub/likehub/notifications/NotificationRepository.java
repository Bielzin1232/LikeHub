package com.likehub.likehub.notifications;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, Long> {


    @Query("SELECT n FROM Notification n " +
            "JOIN FETCH n.sender " +
            "WHERE n.recipient.publicId = :recipientPublicId")
    Slice<Notification> findByRecipientPublicId(@Param("recipientPublicId") UUID recipientPublicId, Pageable pageable);


    @Query("SELECT COUNT(n) FROM Notification n " +
            "WHERE n.recipient.publicId = :recipientPublicId AND n.read = false")
    long countUnreadByRecipientPublicId(@Param("recipientPublicId") UUID recipientPublicId);


    @Modifying
    @Query("UPDATE Notification n SET n.read = true " +
            "WHERE n.recipient.publicId = :recipientPublicId AND n.read = false")
    void markAllAsRead(@Param("recipientPublicId") UUID recipientPublicId);


    @Modifying
    @Query("UPDATE Notification n SET n.read = true " +
            "WHERE n.id = :notificationId AND n.recipient.publicId = :recipientPublicId")
    void markAsRead(@Param("notificationId") Long notificationId, @Param("recipientPublicId") UUID recipientPublicId);
}
