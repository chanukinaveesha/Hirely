package com.recruitsystem.service.notification;

import com.recruitsystem.entity.notification.Notification;
import com.recruitsystem.exception.ResourceNotFoundException;
import com.recruitsystem.repository.notification.NotificationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Persists notifications to the database only. Real email/SMS dispatch is
 * deferred to a later phase.
 */
@Service
@RequiredArgsConstructor
public class PersistingNotificationService implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public Notification notify(Long recipientUserId, String type, String message) {
        Notification notification = Notification.builder()
                .recipientUserId(recipientUserId)
                .type(type)
                .message(message)
                .read(false)
                .build();
        return notificationRepository.save(notification);
    }

    @Override
    public List<Notification> getNotificationsForUser(Long recipientUserId) {
        return notificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(recipientUserId);
    }

    @Override
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));
        notification.setRead(true);
        notificationRepository.save(notification);
    }
}
