package com.likehub.likehub.notifications;

import com.likehub.likehub.auth.commons.api.ApiPaths;
import com.likehub.likehub.notifications.dto.NotificationResponse;
import com.likehub.likehub.users.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // Rota final: GET /api/v1/notifications?page=0
    @GetMapping(ApiPaths.Notifications.BASE)
    public ResponseEntity<List<NotificationResponse>> list(
            @AuthenticationPrincipal User authenticatedUser,
            @RequestParam(name = "page", defaultValue = "0") int page) {

        List<NotificationResponse> notifications = notificationService.listNotifications(authenticatedUser.getPublicId(), page);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping(ApiPaths.Notifications.UNREAD_COUNT)
    public ResponseEntity<Long> getUnreadCount(@AuthenticationPrincipal User authenticatedUser) {
        long count = notificationService.getUnreadCount(authenticatedUser.getPublicId());
        return ResponseEntity.ok(count);
    }

    @PostMapping(ApiPaths.Notifications.READ_ALL)
    public ResponseEntity<Void> readAll(@AuthenticationPrincipal User authenticatedUser) {
        notificationService.markAllAsRead(authenticatedUser.getPublicId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(ApiPaths.Notifications.READ)
    public ResponseEntity<Void> readSingle(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal User authenticatedUser) {

        notificationService.markAsRead(notificationId, authenticatedUser.getPublicId());
        return ResponseEntity.noContent().build();
    }
}
