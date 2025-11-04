package com.example.slices.models;

public class InvitationLogEntry extends LogEntry {
    private int notificationId;
    private int senderId;
    private int recipientId;

    private int eventId;

    private boolean read;

    private boolean accepted;
    private boolean declined;

    public InvitationLogEntry(){
        this.type = LogType.INVITATION;
        this.read = false;
    }
    public InvitationLogEntry(Invitation invitation, int logId) {
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
    public int getNotificationId() {
        return notificationId;
    }
    public int getSenderId() {
        return senderId;
    }
    public int getRecipientId() {
        return recipientId;
    }
    public int getEventId() {
        return eventId;
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

    public boolean isAccepted() {
        return accepted;
    }
    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }
    public boolean isDeclined() {
        return declined;
    }
    public void setDeclined(boolean declined) {
        this.declined = declined;
    }
    public void setEventId(int eventId) {
        this.eventId = eventId;
    }
    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }
    public void setRecipientId(int recipientId) {
        this.recipientId = recipientId;
    }
    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

}
