package com.likehub.likehub.notifications;

import com.likehub.likehub.notifications.dto.NotificationResponse;
import com.likehub.likehub.shared.exceptions.RecursoNaoEncontradoException;
import com.likehub.likehub.users.User;
import com.likehub.likehub.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public void sendNotification(UUID recipientPublicId, UUID senderPublicId, String message, NotificationType type, UUID targetId) {
        if (recipientPublicId.equals(senderPublicId)) {
            return;
        }

        User recipient = userRepository.findByPublicId(recipientPublicId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("DESTINATARIO_NAO_ENCONTRADO", "O destinatário informado não existe."));

        User sender = userRepository.findByPublicId(senderPublicId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("REMETENTE_NAO_ENCONTRADO", "O remetente informado não existe."));

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setSender(sender);
        notification.setMessage(message);
        notification.setType(type);
        notification.setTargetId(targetId);
        notification.setRead(false);

        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> listNotifications(UUID recipientPublicId, int page) {
        Slice<Notification> notifications = notificationRepository.findByRecipientPublicId(
                recipientPublicId,
                PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        return notifications.getContent().stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(UUID recipientPublicId) {
        return notificationRepository.countUnreadByRecipientPublicId(recipientPublicId);
    }

    @Transactional
    public void markAllAsRead(UUID recipientPublicId) {
        notificationRepository.markAllAsRead(recipientPublicId);
    }

    @Transactional
    public void markAsRead(Long notificationId, UUID recipientPublicId) {
        notificationRepository.markAsRead(notificationId, recipientPublicId);
    }
}