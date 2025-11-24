package com.example.slices.models;

/**
 * Log entry representing a general notification
 * @author Ryan Haubrich
 * @version 1.0
 */
public class NotificationLogEntry extends LogEntry {
    /**
     * ID of the associated notification
     */
    private String notificationId;

    /**
     * ID of the sender entrant
     */
    private int senderId;

    /**
     * ID of the recipient entrant
     */
    private int recipientId;

    /**
     * Whether the notification has been read
     */
    private boolean read;

    /**
     * Default constructor for NotificationLogEntry
     * Sets type to NOTIFICATION and read status to false
     */
    public NotificationLogEntry() {
        this.type = LogType.NOTIFICATION;
        this.read = false;
    }

    /**
     * Constructor for NotificationLogEntry from a Notification object
     * @param notification
     *      Notification object to log
     * @param logId
     *      ID of the log entry
     */
    public NotificationLogEntry(Notification notification, String logId) {
        this.message = notification.getTitle() + " " + notification.getBody();
        this.timestamp = notification.getTimestamp();
        this.notificationId = notification.getId();
        this.senderId = notification.getSenderId();
        this.recipientId = notification.getRecipientId();
        this.type = LogType.NOTIFICATION;
        this.logId = logId;
        this.read = notification.getRead();
    }

    /**
     * Getter for the notification ID
     * @return
     *      ID of the associated notification
     */
    public String getNotificationId() {
        return notificationId;
    }

    /**
     * Getter for the sender ID
     * @return
     *      ID of the sender entrant
     */
    public int getSenderId() {
        return senderId;
    }

    /**
     * Getter for the recipient ID
     * @return
     *      ID of the recipient entrant
     */
    public int getRecipientId() {
        return recipientId;
    }

    /**
     * Getter for read status
     * @return
     *      True if the notification has been read, false otherwise
     */
    public boolean isRead() {
        return read;
    }

    /**
     * Setter for read status
     * @param read
     *      Whether the notification has been read
     */
    public void setRead(boolean read) {
        this.read = read;
    }

    /**
     * Marks the log entry as read
     */
    public void markAsRead() {
        this.read = true;
    }
}
