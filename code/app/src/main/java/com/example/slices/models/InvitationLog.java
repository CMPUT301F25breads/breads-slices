package com.example.slices.models;

public class InvitationLog extends Log {
    private int notificationId;
    private int senderId;
    private int recipientId;

    private int eventId;

    private boolean read;

    private boolean accepted;
    private boolean declined;

    public InvitationLog (Invitation invitation, int logId) {
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
        this.read = invitation.isRead();
    }
}
