package com.example.slices.models;

import com.google.firebase.Timestamp;

public class AdminNotification {

    private String title;
    private String body;
    private String id;

    private Long recipientId;
    private Long senderId;
    private Long eventId;

    private Boolean read;

    private Timestamp timestamp;

    private NotificationType type;

    public AdminNotification() {}

    public AdminNotification(String title, String body, String id, Long recipientId, Long senderId) {
        this.title = title;
        this.body = body;
        this.id = id;
        this.recipientId = recipientId;
        this.senderId = senderId;
        this.read = false;
        this.timestamp = Timestamp.now();
        this.type = NotificationType.NOTIFICATION;
    }

    public String getTitle() { return title; }
    public String getBody() { return body; }
    public String getId() { return id; }
    public Long getRecipientId() { return recipientId; }
    public Long getSenderId() { return senderId; }
    public Long getEventId() { return eventId; }
    public Boolean getRead() { return read; }
    public Timestamp getTimestamp() { return timestamp; }
    public NotificationType getType() { return type; }

    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setBody(String body) { this.body = body; }
    public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    public void setRead(Boolean read) { this.read = read; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    public void setType(String typeStr) {
        try {
            this.type = NotificationType.valueOf(typeStr);
        } catch (Exception e) {
            this.type = NotificationType.NOTIFICATION;
        }
    }
}