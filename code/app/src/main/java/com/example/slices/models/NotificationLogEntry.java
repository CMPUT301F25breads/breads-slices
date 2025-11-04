package com.example.slices.models;

public class NotificationLogEntry extends LogEntry {
    private int notificationId;
    private int senderId;
    private int recipientId;

    private boolean read;

    public NotificationLogEntry(){
        this.type = LogType.NOTIFICATION;
        this.read = false;
    }
    public NotificationLogEntry(Notification notification, int logId) {
        this.message = notification.getTitle() + " " + notification.getBody();
        this.timestamp = notification.getTimestamp();
        this.notificationId = notification.getId();
        this.senderId = notification.getSenderId();
        this.recipientId = notification.getRecipientId();
        this.type = LogType.NOTIFICATION;
        this.logId = logId;
        this.read = notification.getRead();

    }

    public int getNotificationId() {
        return notificationId;
    }
    public int getSenderId() {
        return senderId;
    }
    public int getRecipientId() {
        return recipientId;
    }
    public boolean isRead() {
        return read;
    }
    public void setRead(boolean read) {
        this.read = read;
    }
    public void markAsRead() {
        this.read = true;
    }


}
