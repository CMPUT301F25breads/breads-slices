package com.example.slices.models;

import com.google.firebase.Timestamp;

public class Invitation extends Notification {
    private int eventId;

    private boolean accepted;
    private boolean declined;


    public Invitation(String title, String body, int notificationId, int recipientId, int senderId, int eventId) {
        this.title = title;
        this.body = body;
        this.notificationId = notificationId;
        this.recipientId = recipientId;
        this.senderId = senderId;
        this.eventId = eventId;
        this.read = false;
        this.timestamp = Timestamp.now();
        this.type = NotificationType.INVITATION;
        this.accepted = false;
        this.declined = false;

        }

    public int getEventId() {
        return eventId;
    }
    public void setEventId(int eventId) {
        this.eventId = eventId;
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




}
