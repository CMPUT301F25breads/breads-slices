package com.example.slices.models;

/**
 * Log entry representing an invitation
 * @author Ryan Haubrich
 * @version 1.0
 */
public class InvitationLogEntry extends LogEntry {
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
     * ID of the associated event
     */
    private int eventId;

    /**
     * Whether the log entry has been read
     */
    private boolean read;

    /**
     * Whether the invitation was accepted
     */
    private boolean accepted;

    /**
     * Whether the invitation was declined
     */
    private boolean declined;

    /**
     * Default constructor for InvitationLogEntry
     * Sets type to INVITATION and read status to false
     */
    public InvitationLogEntry() {
        this.type = LogType.INVITATION;
        this.read = false;
    }

    /**
     * Constructor for InvitationLogEntry from an Invitation object
     * @param invitation
     *      Invitation object to log
     * @param logId
     *      ID of the log entry
     */
    public InvitationLogEntry(Invitation invitation, String logId) {
        this.message = invitation.getTitle() + " " + invitation.getBody();
        this.timestamp = invitation.getTimestamp();
        this.notificationId = invitation.getId();
        this.senderId = invitation.getSenderId();
        this.recipientId = invitation.getRecipientId();
        this.eventId = invitation.getEventId();
        this.accepted = invitation.isAccepted();
        this.declined = invitation.isDeclined();
        this.type = LogType.INVITATION;
        this.logId = logId;
        this.read = invitation.getRead();
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
     * Setter for the notification ID
     * @param notificationId
     *      ID of the associated notification
     */
    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
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
     * Setter for the sender ID
     * @param senderId
     *      ID of the sender entrant
     */
    public void setSenderId(int senderId) {
        this.senderId = senderId;
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
     * Setter for the recipient ID
     * @param recipientId
     *      ID of the recipient entrant
     */
    public void setRecipientId(int recipientId) {
        this.recipientId = recipientId;
    }

    /**
     * Getter for the event ID
     * @return
     *      ID of the associated event
     */
    public int getEventId() {
        return eventId;
    }

    /**
     * Setter for the event ID
     * @param eventId
     *      ID of the associated event
     */
    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    /**
     * Getter for read status
     * @return
     *      True if the log entry has been read, false otherwise
     */
    public boolean isRead() {
        return read;
    }

    /**
     * Setter for read status
     * @param read
     *      Whether the log entry has been read
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

    /**
     * Getter for accepted status
     * @return
     *      True if the invitation was accepted, false otherwise
     */
    public boolean isAccepted() {
        return accepted;
    }

    /**
     * Setter for accepted status
     * @param accepted
     *      Whether the invitation was accepted
     */
    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    /**
     * Getter for declined status
     * @return
     *      True if the invitation was declined, false otherwise
     */
    public boolean isDeclined() {
        return declined;
    }

    /**
     * Setter for declined status
     * @param declined
     *      Whether the invitation was declined
     */
    public void setDeclined(boolean declined) {
        this.declined = declined;
    }
}