package com.recruitsystem.service.notification;

import com.recruitsystem.entity.notification.Notification;
import java.util.List;

public interface NotificationService {

    Notification notify(Long recipientUserId, String type, String message);

    List<Notification> getNotificationsForUser(Long recipientUserId);

    void markAsRead(Long notificationId);
}
