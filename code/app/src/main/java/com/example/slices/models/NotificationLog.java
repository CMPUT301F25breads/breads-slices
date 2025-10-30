package com.example.slices.models;

public class NotificationLog extends Log {
    private int notificationId;
    private int senderId;
    private int recipientId;

    private boolean read;


    public NotificationLog(Notification notification, int logId) {
        this.message = notification.getTitle() + " " + notification.getBody();
        this.timestamp = notification.getTimestamp();
        this.notificationId = notification.getId();
        this.senderId = notification.getSenderId();
        this.recipientId = notification.getRecipientId();
        this.type = LogType.NOTIFICATION;
        this.logId = logId;
        this.read = notification.isRead();

    }

}
